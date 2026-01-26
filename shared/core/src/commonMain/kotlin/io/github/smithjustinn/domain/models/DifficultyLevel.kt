package io.github.smithjustinn.domain.models

import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.difficulty_casual
import io.github.smithjustinn.resources.difficulty_elephant
import io.github.smithjustinn.resources.difficulty_grandmaster
import io.github.smithjustinn.resources.difficulty_master
import io.github.smithjustinn.resources.difficulty_shark
import io.github.smithjustinn.resources.difficulty_toddler
import org.jetbrains.compose.resources.StringResource

/**
 * Represents a difficulty level with a name resource and pair count.
 */
data class DifficultyLevel(
    val nameRes: StringResource,
    val pairs: Int,
) {
    companion object {
        val defaultLevels =
            listOf(
                DifficultyLevel(Res.string.difficulty_toddler, 6),
                DifficultyLevel(Res.string.difficulty_casual, 8),
                DifficultyLevel(Res.string.difficulty_master, 10),
                DifficultyLevel(Res.string.difficulty_shark, 12),
                DifficultyLevel(Res.string.difficulty_grandmaster, 14),
                DifficultyLevel(Res.string.difficulty_elephant, 16),
            )
    }
}
