package io.github.smithjustinn.domain

/**
 * Logic specific to Time Attack mode.
 */
object TimeAttackLogic {
    private const val DIFF_LEVEL_6 = 6
    private const val DIFF_LEVEL_8 = 8
    private const val DIFF_LEVEL_10 = 10
    private const val DIFF_LEVEL_12 = 12
    const val TIME_PENALTY_MISMATCH = 2L
    private const val INITIAL_TIME_6 = 25L
    private const val INITIAL_TIME_8 = 35L
    private const val INITIAL_TIME_10 = 45L
    private const val INITIAL_TIME_12 = 55L
    private const val TIME_PER_PAIR_FALLBACK = 4
    private const val BASE_TIME_GAIN = 3
    private const val COMBO_TIME_BONUS_MULTIPLIER = 2

    /**
     * Calculates the initial time for Time Attack mode based on the pair count.
     */
    fun calculateInitialTime(pairCount: Int): Long =
        when (pairCount) {
            DIFF_LEVEL_6 -> INITIAL_TIME_6
            DIFF_LEVEL_8 -> INITIAL_TIME_8
            DIFF_LEVEL_10 -> INITIAL_TIME_10
            DIFF_LEVEL_12 -> INITIAL_TIME_12
            else -> (pairCount * TIME_PER_PAIR_FALLBACK).toLong()
        }

    /**
     * Logic for calculating time gain based on combo.
     */
    fun calculateTimeGain(comboMultiplier: Int): Int {
        val baseGain = BASE_TIME_GAIN
        val comboBonus = (comboMultiplier - 1) * COMBO_TIME_BONUS_MULTIPLIER
        return baseGain + comboBonus
    }
}
