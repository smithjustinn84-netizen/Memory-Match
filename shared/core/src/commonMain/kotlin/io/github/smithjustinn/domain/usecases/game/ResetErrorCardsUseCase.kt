package io.github.smithjustinn.domain.usecases.game

import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * Use case to reset cards that were marked as errors (mismatched).
 */
open class ResetErrorCardsUseCase {
    open operator fun invoke(state: MemoryGameState): MemoryGameState = MemoryGameLogic.resetErrorCards(state)
}
