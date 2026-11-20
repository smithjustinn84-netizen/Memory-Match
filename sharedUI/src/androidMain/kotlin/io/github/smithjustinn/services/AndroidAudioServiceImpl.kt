package io.github.smithjustinn.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.SoundPool
import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.services.AudioService.Companion.toResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class AndroidAudioServiceImpl(
    private val context: Context,
    private val logger: Logger,
    settingsRepository: SettingsRepository,
) : AudioService {
    // MediaPlayer is not thread-safe: every player call and every field below is touched only on
    // the main thread. File and resource work hops to IO explicitly.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val soundPool =
        SoundPool
            .Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).build()

    private val soundMap = ConcurrentHashMap<StringResource, Int>()
    private val resourceToName = ConcurrentHashMap<StringResource, String>()
    private val loadedSounds = ConcurrentHashMap.newKeySet<Int>()
    private val fallbackPlayers = ConcurrentHashMap<StringResource, MediaPlayer>()
    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var isMusicRequested = false
    private var soundVolume = 1.0f
    private var musicFileWritten = false
    private val musicFader = MusicFader(scope) { createMusicTrack() }

    init {
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
            }.launchIn(scope)

        settingsRepository.musicVolume
            .onEach { musicFader.targetVolume = it }
            .launchIn(scope)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }

        scope.launch {
            AudioService.SoundEffect.entries.forEach { effect ->
                loadSound(effect.toResource())
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadSound(resource: StringResource): Int? =
        try {
            val name = getString(resource)
            resourceToName[resource] = name
            val fileName = "$name.m4a"
            val id =
                withContext(Dispatchers.IO) {
                    val bytes = Res.readBytes("files/$fileName")
                    val tempFile = File(context.cacheDir, fileName)
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    soundPool.load(tempFile.absolutePath, 1)
                }
            soundMap[resource] = id
            id
        } catch (e: IOException) {
            logger.e(e) { "IO Error loading sound resource: $resource" }
            null
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Unexpected error loading sound resource: $resource" }
            null
        }

    private fun playSound(
        resource: StringResource,
        rate: Float,
    ) {
        if (!isSoundEnabled) return

        val soundId = soundMap[resource]
        if (soundId != null && loadedSounds.contains(soundId)) {
            val streamId = soundPool.play(soundId, soundVolume, soundVolume, 1, 0, rate)
            if (streamId == 0) {
                logger.w { "SoundPool play failed, using fallback for: $resource" }
                playFallback(resource, rate)
            }
        } else {
            // Sound not loaded yet - wait briefly and retry before falling back
            scope.launch {
                delay(SOUNDPOOL_RETRY_DELAY_MS)
                val retrySoundId = soundMap[resource]
                if (retrySoundId != null && loadedSounds.contains(retrySoundId)) {
                    soundPool.play(retrySoundId, soundVolume, soundVolume, 1, 0, rate)
                } else {
                    logger.w { "SoundPool not ready after retry, using fallback for: $resource" }
                    playFallback(resource, rate)
                }
            }
        }
    }

    private fun playFallback(
        resource: StringResource,
        rate: Float,
    ) {
        val name = resourceToName[resource] ?: return
        val fileName = "$name.m4a"
        scope.launch {
            try {
                val tempFile = File(context.cacheDir, fileName)
                if (withContext(Dispatchers.IO) { tempFile.exists() }) {
                    val player = fallbackPlayers[resource]
                    if (player != null) {
                        try {
                            if (player.isPlaying) player.seekTo(0)
                            player.startAt(rate)
                        } catch (e: IllegalStateException) {
                            logger.w(e) { "Fallback player not ready or in bad state: $name" }
                        }
                    } else {
                        createNewFallbackPlayer(resource, tempFile, name, rate)
                    }
                }
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                logger.e(e) { "Error playing fallback sound: $name" }
                fallbackPlayers.remove(resource)?.release()
            }
        }
    }

    private fun createNewFallbackPlayer(
        resource: StringResource,
        tempFile: File,
        name: String,
        rate: Float,
    ) {
        var newPlayer: MediaPlayer? = null
        try {
            newPlayer = MediaPlayer()
            newPlayer.apply {
                setDataSource(tempFile.absolutePath)
                setVolume(soundVolume, soundVolume)
                setOnCompletionListener { mp -> mp.seekTo(0) }
                setOnPreparedListener { mp -> mp.startAt(rate) }
                prepareAsync()
            }
            fallbackPlayers[resource] = newPlayer
        } catch (e: IOException) {
            logger.e(e) { "IO Error initializing fallback player: $name" }
            newPlayer?.release()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Unexpected error initializing fallback player: $name" }
            newPlayer?.release()
            fallbackPlayers.remove(resource)?.release()
        }
    }

    /**
     * Plays from the current position at [rate], changing speed and pitch together as SoundPool
     * does. Must be called once the player is prepared.
     */
    private fun MediaPlayer.startAt(rate: Float) {
        playbackParams = PlaybackParams().setSpeed(rate).setPitch(rate)
        if (!isPlaying) start()
    }

    override fun playEffect(
        effect: AudioService.SoundEffect,
        rate: Float,
    ) {
        playSound(effect.toResource(), rate.coerceIn(MIN_RATE, MAX_RATE))
    }

    override fun startMusic() {
        isMusicRequested = true
        updateMusicPlayback()
    }

    override fun stopMusic() {
        isMusicRequested = false
        updateMusicPlayback()
    }

    private fun updateMusicPlayback() {
        musicFader.setPlaying(isMusicRequested && isMusicEnabled)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun createMusicTrack(): MusicTrack? =
        try {
            val name = getString(AudioService.MUSIC)
            val fileName = "$name.m4a"
            val tempFile = File(context.cacheDir, fileName)
            withContext(Dispatchers.IO) {
                // Rewrite once per process so an app update's new music replaces the cached copy.
                if (!musicFileWritten || !tempFile.exists()) {
                    val bytes = Res.readBytes("files/$fileName")
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    musicFileWritten = true
                }
            }
            AndroidMusicTrack(tempFile)
        } catch (e: IOException) {
            logger.e(e) { "IO Error starting music" }
            null
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Unexpected error starting music" }
            null
        }

    /** Looping MediaPlayer that defers play() until async preparation finishes. Main thread only. */
    private inner class AndroidMusicTrack(
        file: File,
    ) : MusicTrack {
        private var isPrepared = false
        private var wantsPlay = false
        private var level = 0f
        private val player =
            MediaPlayer().apply {
                setDataSource(file.absolutePath)
                isLooping = true
                setVolume(0f, 0f)
                setOnPreparedListener { mp ->
                    isPrepared = true
                    mp.setVolume(level, level)
                    if (wantsPlay) mp.start()
                }
                prepareAsync()
            }

        override fun play() {
            wantsPlay = true
            if (isPrepared && !player.isPlaying) player.start()
        }

        override fun setVolume(volume: Float) {
            level = volume
            if (isPrepared) player.setVolume(volume, volume)
        }

        override fun release() {
            // stop() is skipped: it throws while preparing, and release() stops playback anyway.
            player.release()
        }
    }

    companion object {
        private const val MAX_STREAMS = 10
        private const val SOUNDPOOL_RETRY_DELAY_MS = 100L
        private const val MIN_RATE = 0.5f
        private const val MAX_RATE = 2f
    }
}
