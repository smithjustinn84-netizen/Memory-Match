package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.services.AudioService.Companion.toResource
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository,
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.Main)

    // Pool of players for each sound effect to support polyphony
    private val playerPools = mutableMapOf<StringResource, MutableList<AVAudioPlayer>>()
    private val poolIndices = mutableMapOf<StringResource, Int>()

    // Completes once each effect's preload has finished, whether or not it succeeded.
    private val preloadDone =
        AudioService.SoundEffect.entries.associate { it.toResource() to CompletableDeferred<Unit>() }

    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var isMusicRequested = false
    private var soundVolume = 1.0f
    private val musicFader = MusicFader(scope) { createMusicTrack() }

    init {
        setupAudioSession()

        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        settingsRepository.soundVolume
            .onEach { volume ->
                soundVolume = volume
                playerPools.values.flatten().forEach { it.volume = volume }
            }.launchIn(scope)

        settingsRepository.isMusicEnabled
            .onEach { enabled ->
                isMusicEnabled = enabled
                updateMusicPlayback()
            }.launchIn(scope)

        settingsRepository.musicVolume
            .onEach { musicFader.targetVolume = it }
            .launchIn(scope)

        preloadSounds()
    }

    private fun setupAudioSession() {
        try {
            val session = AVAudioSession.sharedInstance()
            // Ambient: respects the silent switch and mixes with whatever the player is listening to.
            session.setCategory(AVAudioSessionCategoryAmbient, error = null)
            session.setActive(true, error = null)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Error setting up AVAudioSession" }
        }
    }

    private fun preloadSounds() {
        scope.launch {
            AudioService.SoundEffect.entries.forEach { effect ->
                val resource = effect.toResource()
                try {
                    val pool =
                        withContext(Dispatchers.Default) {
                            val name = getString(resource)
                            val path = "$name.m4a"
                            val bytes = Res.readBytes("files/$path")
                            if (bytes.isEmpty()) {
                                logger.w { "Sound file is empty: $name" }
                                return@withContext null
                            }
                            val data =
                                bytes.usePinned { pinned ->
                                    NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                                }

                            // Create a pool of players for polyphony support
                            val localPool = mutableListOf<AVAudioPlayer>()
                            repeat(PLAYER_POOL_SIZE) {
                                val player = AVAudioPlayer(data = data, error = null)
                                player.volume = soundVolume
                                player.enableRate = true // must be set before prepareToPlay
                                player.prepareToPlay()
                                localPool.add(player)
                            }
                            localPool
                        }

                    if (pool != null) {
                        playerPools[resource] = pool
                        poolIndices[resource] = 0
                    }
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    logger.e(e) { "Error pre-loading sound effect: $effect" }
                } finally {
                    preloadDone[resource]?.complete(Unit)
                }
            }
        }
    }

    private fun playSound(
        resource: StringResource,
        rate: Float,
    ) {
        if (!isSoundEnabled) return

        val pool = playerPools[resource]
        if (pool.isNullOrEmpty()) {
            playWhenLoaded(resource, rate)
        } else {
            playFromPool(pool, resource, rate)
        }
    }

    /**
     * Plays an effect requested before its preload finished, if the preload finishes within
     * [LOAD_WAIT_MS]. Any later and the sound would land too far from what triggered it, so it is
     * dropped with a warning.
     */
    private fun playWhenLoaded(
        resource: StringResource,
        rate: Float,
    ) {
        val done = preloadDone[resource] ?: return
        scope.launch {
            val finished = withTimeoutOrNull(LOAD_WAIT_MS) { done.await() } != null
            val pool = playerPools[resource]
            when {
                !pool.isNullOrEmpty() -> if (isSoundEnabled) playFromPool(pool, resource, rate)
                finished -> logger.w { "Sound failed to load, not played: $resource" }
                else -> logger.w { "Sound not loaded within $LOAD_WAIT_MS ms, not played: $resource" }
            }
        }
    }

    private fun playFromPool(
        pool: List<AVAudioPlayer>,
        resource: StringResource,
        rate: Float,
    ) {
        // Round-robin player selection for polyphony
        val currentIndex = poolIndices[resource] ?: 0
        val player = pool[currentIndex]

        // Stop and rewind if already playing
        if (player.playing) {
            player.stop()
            player.currentTime = 0.0
        }

        player.rate = rate
        player.play()

        // Update index for next call
        poolIndices[resource] = (currentIndex + 1) % pool.size
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

    private suspend fun createMusicTrack(): MusicTrack? =
        try {
            withContext(Dispatchers.Default) {
                val name = getString(AudioService.MUSIC)
                val bytes = Res.readBytes("files/$name.m4a")
                if (bytes.isEmpty()) return@withContext null
                val data =
                    bytes.usePinned { pinned ->
                        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                    }
                AVAudioPlayer(data = data, error = null).apply {
                    numberOfLoops = -1
                    volume = 0f
                    prepareToPlay()
                }
            }?.let(::IosMusicTrack)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Error starting music" }
            null
        }

    private class IosMusicTrack(
        private val player: AVAudioPlayer,
    ) : MusicTrack {
        override fun play() {
            if (!player.playing) player.play()
        }

        override fun setVolume(volume: Float) {
            player.volume = volume
        }

        override fun release() {
            player.stop()
        }
    }

    companion object {
        private const val PLAYER_POOL_SIZE = 4
        private const val LOAD_WAIT_MS = 250L
        private const val MIN_RATE = 0.5f
        private const val MAX_RATE = 2f
    }
}
