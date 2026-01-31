package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * High Roller Circuit-specific game logic.
 */
object HighRollerLogic {
    /**
     * Calculates pot growth, banking events, and score for High Roller mode.
     */
    fun calculateHighRollerScore(
        state: MemoryGameState,
        isWon: Boolean,
        matchBasePoints: Int,
        matchComboBonus: Int,
    ): HighRollerScoreResult {
        val config = state.config
        val matchPoints = matchBasePoints + matchComboBonus
        val pointsEarned = matchPoints * state.circuitStage.potGrowthMultiplier

        val newPot = state.currentPot + pointsEarned
        val isNutsBanking = (state.comboMultiplier + 1) >= config.theNutsThreshold

        val pointsToBank = if (isWon || isNutsBanking) newPot else 0
        val resultingBankedScore = state.bankedScore + pointsToBank
        val resultingPot = if (isWon || isNutsBanking) 0 else newPot

        val finalScore =
            if (isWon && state.isDoubleDownActive) {
                resultingBankedScore * 2
            } else {
                resultingBankedScore
            }
        val ddBonus = finalScore - resultingBankedScore

        return HighRollerScoreResult(
            finalScore = finalScore,
            resultingPot = resultingPot,
            resultingBankedScore = resultingBankedScore,
            ddBonus = ddBonus,
        )
    }

    /**
     * Calculates the penalty applied to the pot on a mismatch (Bad Beat).
     */
    fun calculateBadBeat(state: MemoryGameState): BadBeatResult {
        val penalty = (state.currentPot * state.circuitStage.bustPenalty).toInt()
        val finalPot = (state.currentPot - penalty).coerceAtLeast(0)
        val isBusted = finalPot <= 0 && state.bankedScore <= 0

        return BadBeatResult(
            finalPot = finalPot,
            isBusted = isBusted,
        )
    }

    data class HighRollerScoreResult(
        val finalScore: Int,
        val resultingPot: Int,
        val resultingBankedScore: Int,
        val ddBonus: Int,
    )

    data class BadBeatResult(
        val finalPot: Int,
        val isBusted: Boolean,
    )
}
