package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Domain model for a single leaderboard entry.
 */
@Serializable
data class LeaderboardEntry(
    val id: Long = 0,
    val pairCount: Int,
    val score: Int,
    val timeSeconds: Long,
    val moves: Int,
    val timestamp: Instant,
    val gameMode: GameMode = GameMode.TIME_ATTACK,
)
