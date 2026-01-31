package io.github.smithjustinn.ui.circuit

import com.arkivanov.decompose.ComponentContext
import io.github.smithjustinn.domain.models.CircuitStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface BuyInComponent {
    val state: StateFlow<BuyInUIState>

    fun onWagerSelected(wager: Int)

    fun onStartRound()

    fun onBack()
}

data class BuyInUIState(
    val stage: CircuitStage = CircuitStage.QUALIFIER,
    val bankedScore: Int = 0,
    val selectedWager: Int = 100,
    val availableWagers: List<Int> = listOf(100, 500, 1000),
)

class DefaultBuyInComponent(
    componentContext: ComponentContext,
    private val stage: CircuitStage,
    private val bankedScore: Int,
    private val onStartRound: (wager: Int) -> Unit,
    private val onBackClicked: () -> Unit,
) : BuyInComponent,
    ComponentContext by componentContext {
    private val _state =
        MutableStateFlow(
            BuyInUIState(
                stage = stage,
                bankedScore = bankedScore,
                availableWagers = calculateAvailableWagers(bankedScore),
            ),
        )
    override val state: StateFlow<BuyInUIState> = _state.asStateFlow()

    override fun onWagerSelected(wager: Int) {
        _state.update { it.copy(selectedWager = wager) }
    }

    override fun onStartRound() {
        onStartRound(_state.value.selectedWager)
    }

    override fun onBack() = onBackClicked()

    private fun calculateAvailableWagers(banked: Int): List<Int> {
        val base = listOf(100, 500, 1000)
        return if (banked ==
            0
        ) {
            listOf(0)
        } else {
            base.filter { it <= banked } +
                if (banked > 0 && banked < 100) listOf(banked) else emptyList()
        }
    }
}
