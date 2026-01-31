package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.CircuitStage
import io.github.smithjustinn.domain.models.GameMode

data class GameArgs(
    val pairCount: Int,
    val mode: GameMode,
    val forceNewGame: Boolean,
    val seed: Long? = null,
    val circuitStage: CircuitStage? = null,
    val bankedScore: Int = 0,
    val currentWager: Int = 0,
)
