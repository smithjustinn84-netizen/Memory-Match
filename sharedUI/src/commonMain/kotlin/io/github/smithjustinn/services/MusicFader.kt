package io.github.smithjustinn.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A platform music player, as [MusicFader] drives it. */
interface MusicTrack {
    /** Starts or resumes looping playback. Safe to call while already playing. */
    fun play()

    fun setVolume(volume: Float)

    /** Stops playback and frees the player. The track is not used again afterwards. */
    fun release()
}

/**
 * Fades the background music in and out, and resolves start/stop requests that arrive mid-fade.
 *
 * Not thread-safe: call it, and run [scope], on a single thread (the main thread on mobile).
 * A start during a fade-out cancels the fade and ramps back up on the same track; the track is
 * released only when a fade-out runs to the end.
 */
class MusicFader(
    private val scope: CoroutineScope,
    private val fadeInMs: Long = DEFAULT_FADE_IN_MS,
    private val fadeOutMs: Long = DEFAULT_FADE_OUT_MS,
    private val createTrack: suspend () -> MusicTrack?,
) {
    private var track: MusicTrack? = null
    private var currentVolume = 0f
    private var fadeJob: Job? = null
    private var isPlaying = false

    /** The user's music volume; the level fades settle on. */
    var targetVolume: Float = 1f
        set(value) {
            field = value
            if (isPlaying && fadeJob?.isActive != true) {
                currentVolume = value
                track?.setVolume(value)
            }
        }

    /** Whether music should be heard. Repeating the current value does nothing. */
    fun setPlaying(playing: Boolean) {
        if (playing == isPlaying) return
        isPlaying = playing
        fadeJob?.cancel()
        fadeJob = scope.launch { if (playing) fadeIn() else fadeOut() }
    }

    private suspend fun fadeIn() {
        // NonCancellable so a stop that lands while the player is being built can't leak it.
        val active =
            track ?: withContext(NonCancellable) { createTrack() }?.also { created ->
                track = created
                currentVolume = 0f
                created.setVolume(0f)
            } ?: run {
                isPlaying = false // let a later start retry
                return
            }
        if (!isPlaying) return
        active.play()
        rampTo(targetVolume, fadeInMs)
        currentVolume = targetVolume
        active.setVolume(targetVolume)
    }

    private suspend fun fadeOut() {
        rampTo(0f, fadeOutMs)
        track?.release()
        track = null
        currentVolume = 0f
    }

    private suspend fun rampTo(
        to: Float,
        durationMs: Long,
    ) {
        val from = currentVolume
        val steps = (durationMs / STEP_MS).coerceAtLeast(1)
        for (i in 1..steps) {
            delay(STEP_MS)
            currentVolume = from + (to - from) * i / steps
            track?.setVolume(currentVolume)
        }
    }

    companion object {
        const val DEFAULT_FADE_IN_MS = 600L
        const val DEFAULT_FADE_OUT_MS = 400L
        private const val STEP_MS = 16L
    }
}
