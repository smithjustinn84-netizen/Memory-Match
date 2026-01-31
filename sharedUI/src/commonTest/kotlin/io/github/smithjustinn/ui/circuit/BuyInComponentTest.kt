package io.github.smithjustinn.ui.circuit

import com.arkivanov.decompose.DefaultComponentContext
import io.github.smithjustinn.domain.models.CircuitStage
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BuyInComponentTest : BaseComponentTest() {
    @Test
    fun `initial state selects correct wager when banked score is 0`() =
        runTest { lifecycle ->
            val component =
                DefaultBuyInComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    stage = CircuitStage.QUALIFIER,
                    bankedScore = 0,
                    onStartRound = {},
                    onBackClicked = {},
                )

            val state = component.state.value
            assertEquals(listOf(0), state.availableWagers)
            assertEquals(
                0,
                state.selectedWager,
                "Selected wager should range to available wager when default (100) is unavailable",
            )
        }

    @Test
    fun `initial state selects default 100 wager when available`() =
        runTest { lifecycle ->
            val component =
                DefaultBuyInComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    stage = CircuitStage.SEMI_FINAL,
                    bankedScore = 200,
                    onStartRound = {},
                    onBackClicked = {},
                )

            val state = component.state.value
            assertEquals(listOf(100), state.availableWagers)
            assertEquals(100, state.selectedWager)
        }

    @Test
    fun `initial state selects banked score if less than 100`() =
        runTest { lifecycle ->
            val component =
                DefaultBuyInComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    stage = CircuitStage.QUALIFIER,
                    bankedScore = 50,
                    onStartRound = {},
                    onBackClicked = {},
                )

            val state = component.state.value
            assertEquals(listOf(50), state.availableWagers)
            assertEquals(50, state.selectedWager)
        }
}
