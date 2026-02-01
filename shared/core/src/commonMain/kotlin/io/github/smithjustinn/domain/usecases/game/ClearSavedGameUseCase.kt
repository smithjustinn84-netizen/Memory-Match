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
    open suspend operator fun invoke(): Result<Unit> =
        runCatching {
            gameStateRepository.clearSavedGameState()
        }.onFailure { e ->
            logger.e(e) { "Failed to clear saved game state via use case" }
        }
}
