package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Difficulty type identifier for the game.
 */
@Serializable
enum class DifficultyType {
    TOURIST,
    CASUAL,
    MASTER,
    SHARK,
}

/**
 * Represents a difficulty level with a type identifier and pair count.
 */
@Serializable
data class DifficultyLevel(
    val type: DifficultyType,
    val pairs: Int,
) {
    companion object {
        val defaultLevels =
            listOf(
                DifficultyLevel(DifficultyType.TOURIST, 6),
                DifficultyLevel(DifficultyType.CASUAL, 8),
                DifficultyLevel(DifficultyType.MASTER, 10),
                DifficultyLevel(DifficultyType.SHARK, 12),
            )
    }
}
