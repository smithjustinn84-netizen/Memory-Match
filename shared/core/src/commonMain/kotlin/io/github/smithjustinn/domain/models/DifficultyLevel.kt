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
    ;

    val payoutMultiplier: Double
        get() =
            when (this) {
                TOURIST -> MULTIPLIER_TOURIST
                CASUAL -> MULTIPLIER_CASUAL
                MASTER -> MULTIPLIER_MASTER
                SHARK -> MULTIPLIER_SHARK
            }

    private companion object {
        const val MULTIPLIER_TOURIST = 0.25
        const val MULTIPLIER_CASUAL = 1.0
        const val MULTIPLIER_MASTER = 2.5
        const val MULTIPLIER_SHARK = 5.0
    }
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
