package io.github.smithjustinn.services

import kotlin.math.pow
import kotlin.random.Random

/** Playback-rate helpers that keep repeated sounds musical rather than mechanical. */
object AudioPitch {
    /** Semitone offsets climbed by successive combo levels: a major-pentatonic run, then held. */
    private val COMBO_STEPS = intArrayOf(0, 2, 4, 7)

    private const val SEMITONES_PER_OCTAVE = 12.0
    private const val JITTER_RANGE = 0.06f
    private const val HALF = 0.5f

    /**
     * Rate for the match sound at [combo], where 1 is the first pair of a streak. The pitch climbs
     * one pentatonic step per extra pair and holds at a perfect fifth (about 1.5x).
     */
    fun comboRate(combo: Int): Float {
        val step = (combo - 1).coerceIn(0, COMBO_STEPS.lastIndex)
        return semitones(COMBO_STEPS[step])
    }

    /** A small random rate change (about ±3%) so rapid repeats like flips don't sound identical. */
    fun jitter(random: Random = Random.Default): Float = 1f + (random.nextFloat() - HALF) * JITTER_RANGE

    private fun semitones(n: Int): Float = 2.0.pow(n / SEMITONES_PER_OCTAVE).toFloat()
}
