package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Domain model for game statistics.
 */
@Serializable
data class GameStats(
    val pairCount: Int,
    val bestScore: Int,
    val bestTimeSeconds: Long,
)
