package io.github.smithjustinn.ui.start

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode

data class DifficultyState(
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.defaultLevels[1],
    val difficulties: List<DifficultyLevel> = DifficultyLevel.defaultLevels,
    val selectedMode: GameMode = GameMode.TIME_ATTACK,
    val cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val areSuitsMultiColored: Boolean = false,
    val hasSavedGame: Boolean = false,
    val savedGamePairCount: Int = 0,
    val savedGameMode: GameMode = GameMode.TIME_ATTACK,
    val savedGameDifficulty: DifficultyType = DifficultyType.CASUAL,
    val isDailyChallengeCompleted: Boolean = false,
    val shouldAnimateEntrance: Boolean = true,
    val activeCircuitRunId: String? = null,
    val activeCircuitBankedScore: Int = 0,
    val totalBalance: Long = 0,
)
