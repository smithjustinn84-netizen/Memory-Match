package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.SavedGame

interface GameStateRepository {
    suspend fun saveGameState(
        gameState: MemoryGameState,
        elapsedTimeSeconds: Long,
    )

    suspend fun getSavedGameState(): SavedGame?

    suspend fun clearSavedGameState()
}
