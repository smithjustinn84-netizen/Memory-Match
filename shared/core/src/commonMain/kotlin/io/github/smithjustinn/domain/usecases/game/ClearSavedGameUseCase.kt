package io.github.smithjustinn.domain.usecases.game

import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.repositories.GameStateRepository

/**
 * Usecase to clear the saved game state.
 */
open class ClearSavedGameUseCase(
    private val gameStateRepository: GameStateRepository,
    private val logger: Logger,
) {
    @Suppress("TooGenericExceptionCaught")
    open suspend operator fun invoke() {
        try {
            gameStateRepository.clearSavedGameState()
        } catch (e: Exception) {
            logger.e(e) { "Failed to clear saved game state via use case" }
        }
    }
}
