package io.github.smithjustinn.domain.models

import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.StringResource

/**
 * Configuration for game scoring and bonuses.
 */
data class ScoringConfig(
    val baseMatchPoints: Int = 20,
    val comboBonusPoints: Int = 50,
    val timeBonusPerPair: Int = 50,
    val timePenaltyPerSecond: Int = 1,
    val moveBonusMultiplier: Int = 10000 // Base for move efficiency
)

/**
 * Breakdown of the final score.
 */
data class ScoreBreakdown(
    val matchPoints: Int = 0,
    val timeBonus: Int = 0,
    val moveBonus: Int = 0,
    val totalScore: Int = 0
)

/**
 * Represents a match comment with its resource and optional arguments.
 */
data class MatchComment(val res: StringResource, val args: List<Any> = emptyList())

/**
 * Represents the core state of the memory game.
 */
data class MemoryGameState(
    val cards: List<CardState> = emptyList(),
    val pairCount: Int = 8,
    val isGameWon: Boolean = false,
    val moves: Int = 0,
    val score: Int = 0,
    val comboMultiplier: Int = 1,
    val matchComment: MatchComment? = null,
    val config: ScoringConfig = ScoringConfig(),
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown()
)

/**
 * Events that can occur during the game, which the UI might want to react to.
 */
sealed class GameDomainEvent {
    data object MatchSuccess : GameDomainEvent()
    data object MatchFailure : GameDomainEvent()
    data object GameWon : GameDomainEvent()
}
