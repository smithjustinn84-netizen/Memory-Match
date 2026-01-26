package io.github.smithjustinn.domain.usecases.game

import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * Use case to calculate and apply final bonuses when a game is won.
 */
open class CalculateFinalScoreUseCase {
    open operator fun invoke(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState = MemoryGameLogic.applyFinalBonuses(state, elapsedTimeSeconds)
}
