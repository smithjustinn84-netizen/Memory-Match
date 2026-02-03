package io.github.smithjustinn.domain.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// Type-specific serializers moved to MemoryGameSerializers.kt

/**
 * Represents the core state of the memory game.
 */
@Serializable
data class MemoryGameState(
    @Serializable(with = CardStateListSerializer::class)
    val cards: ImmutableList<CardState> = persistentListOf(),
    val pairCount: Int = 8,
    val isGameWon: Boolean = false,
    val isGameOver: Boolean = false,
    val moves: Int = 0,
    val score: Int = 0,
    val currentPot: Int = 0,
    val totalBasePoints: Int = 0,
    val totalComboBonus: Int = 0,
    val totalDoubleDownBonus: Int = 0,
    val comboMultiplier: Int = 0,
    val isDoubleDownActive: Boolean = false,
    val isBusted: Boolean = false,
    @Transient val matchComment: MatchComment? = null,
    val config: ScoringConfig = ScoringConfig(),
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown(),
    val mode: GameMode = GameMode.TIME_ATTACK,
    @Serializable(with = IntListSerializer::class)
    val lastMatchedIds: ImmutableList<Int> = persistentListOf(),
    val seed: Long? = null,
    val activeMutators: Set<DailyChallengeMutator> = emptySet(),
    val difficulty: DifficultyType = DifficultyType.CASUAL,
) {
    init {
        require(pairCount > 0) { "Pair count must be positive" }
        require(moves >= 0) { "Moves cannot be negative" }
        require(comboMultiplier >= 0) { "Combo multiplier cannot be negative" }
    }
}
