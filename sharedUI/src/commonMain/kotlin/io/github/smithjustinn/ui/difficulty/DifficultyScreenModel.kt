package io.github.smithjustinn.ui.difficulty

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.repositories.GameStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the state of the difficulty selection screen.
 */
data class DifficultyState(
    val difficulties: List<DifficultyLevel> = DifficultyLevel.defaultLevels,
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.defaultLevels[1],
    val hasSavedGame: Boolean = false,
    val savedGamePairCount: Int = 0,
    val savedGameMode: GameMode = GameMode.STANDARD,
    val selectedMode: GameMode = GameMode.STANDARD
)

/**
 * Sealed class representing user intents for the difficulty screen.
 */
sealed class DifficultyIntent {
    data class SelectDifficulty(val level: DifficultyLevel) : DifficultyIntent()
    data class SelectMode(val mode: GameMode) : DifficultyIntent()
    data class StartGame(val pairs: Int, val mode: GameMode) : DifficultyIntent()
    data object CheckSavedGame : DifficultyIntent()
    data object ResumeGame : DifficultyIntent()
}

@Inject
class DifficultyScreenModel(
    private val gameStateRepository: GameStateRepository,
    private val logger: Logger
) : ScreenModel {
    private val _state = MutableStateFlow(DifficultyState())
    val state: StateFlow<DifficultyState> = _state.asStateFlow()

    fun handleIntent(intent: DifficultyIntent, onNavigate: (Int, GameMode) -> Unit = { _, _ -> }) {
        when (intent) {
            is DifficultyIntent.SelectDifficulty -> {
                _state.update { it.copy(selectedDifficulty = intent.level) }
            }
            is DifficultyIntent.SelectMode -> {
                _state.update { it.copy(selectedMode = intent.mode) }
            }
            is DifficultyIntent.StartGame -> {
                onNavigate(intent.pairs, intent.mode)
            }
            is DifficultyIntent.CheckSavedGame -> {
                screenModelScope.launch {
                    try {
                        val savedGame = gameStateRepository.getSavedGameState()
                        _state.update { 
                            it.copy(
                                hasSavedGame = savedGame != null && !savedGame.first.isGameOver,
                                savedGamePairCount = savedGame?.first?.pairCount ?: 0,
                                savedGameMode = savedGame?.first?.mode ?: GameMode.STANDARD
                            ) 
                        }
                    } catch (e: Exception) {
                        logger.e(e) { "Error checking for saved game" }
                    }
                }
            }
            is DifficultyIntent.ResumeGame -> {
                if (_state.value.hasSavedGame) {
                    onNavigate(_state.value.savedGamePairCount, _state.value.savedGameMode)
                }
            }
        }
    }
}
