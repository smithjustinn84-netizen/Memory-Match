package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.services.AudioService.Companion.toResource
import javafx.application.Platform
import javafx.scene.media.AudioClip
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class JvmAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository,
) : AudioService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // All music state lives on this single-threaded dispatcher, as MusicFader requires.
    private val musicScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))

    // AudioClip is low-latency and polyphonic: overlapping flips don't cut each other off.
    private val clips = ConcurrentHashMap<StringResource, AudioClip>()

    @Volatile private var isSoundEnabled = true

    @Volatile private var soundVolume = 1.0f
    private var isMusicEnabled = true
    private var isMusicRequested = false
    private val musicFader = MusicFader(musicScope) { createMusicTrack() }

    private val tempCacheDir =
        File(System.getProperty("java.io.tmpdir"), "memory_match_audio").apply {
            if (!exists()) mkdirs()
        }

    init {
        // Ensure JavaFX platform is initialized
        // Using Platform.startup now that we have all usage dependencies (base, graphics, controls)
        try {
            Platform.startup {}
        } catch (e: IllegalStateException) {
            // Already initialized - safe to ignore
            logger.d(e) { "JavaFX Platform already initialized" }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Error initializing JavaFX toolkit" }
        }

        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        settingsRepository.soundVolume
            .onEach { soundVolume = it }
            .launchIn(scope)

        settingsRepository.isMusicEnabled
            .onEach { enabled ->
                isMusicEnabled = enabled
                updateMusicPlayback()
            }.launchIn(musicScope)

        settingsRepository.musicVolume
            .onEach { musicFader.targetVolume = it }
            .launchIn(musicScope)

        scope.launch {
            AudioService.SoundEffect.entries.forEach { effect ->
                loadClip(effect.toResource())
            }
        }
    }

    /**
     * Copies a bundled sound to the temp cache (JavaFX needs a URI). The name carries a content
     * hash, so an update that changes a sound never plays a stale cached copy.
     */
    private suspend fun getAudioFile(resource: StringResource): File {
        val name = getString(resource)
        val bytes = Res.readBytes("files/$name.m4a")
        val tag = "${bytes.size.toString(HEX)}-${bytes.contentHashCode().toUInt().toString(HEX)}"
        val tempFile = File(tempCacheDir, "$name-$tag.m4a")
        if (!tempFile.exists()) {
            tempFile.writeBytes(bytes)
        }
        return tempFile
    }

    private suspend fun loadClip(resource: StringResource) {
        withContext(Dispatchers.IO) {
            try {
                clips[resource] = AudioClip(getAudioFile(resource).toURI().toString())
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                logger.e(e) { "Error pre-loading sound resource: $resource" }
            }
        }
    }

    override fun playEffect(
        effect: AudioService.SoundEffect,
        rate: Float,
    ) {
        if (!isSoundEnabled) return
        val clip = clips[effect.toResource()] ?: return
        clip.play(
            soundVolume.toDouble(),
            0.0,
            rate.coerceIn(MIN_RATE, MAX_RATE).toDouble(),
            0.0,
            0,
        )
    }

    override fun startMusic() {
        musicScope.launch {
            isMusicRequested = true
            updateMusicPlayback()
        }
    }

    override fun stopMusic() {
        musicScope.launch {
            isMusicRequested = false
            updateMusicPlayback()
        }
    }

    private fun updateMusicPlayback() {
        musicFader.setPlaying(isMusicRequested && isMusicEnabled)
    }

    private suspend fun createMusicTrack(): MusicTrack? =
        try {
            val file = withContext(Dispatchers.IO) { getAudioFile(AudioService.MUSIC) }
            JvmMusicTrack(MediaPlayer(Media(file.toURI().toString())))
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Error starting music" }
            null
        }

    private class JvmMusicTrack(
        private val player: MediaPlayer,
    ) : MusicTrack {
        init {
            player.cycleCount = MediaPlayer.INDEFINITE
            player.volume = 0.0
        }

        override fun play() {
            if (player.status != MediaPlayer.Status.PLAYING) player.play()
        }

        override fun setVolume(volume: Float) {
            player.volume = volume.toDouble()
        }

        override fun release() {
            player.stop()
            player.dispose()
        }
    }

    private companion object {
        const val MIN_RATE = 0.5f
        const val MAX_RATE = 2f
        const val HEX = 16
    }
}
