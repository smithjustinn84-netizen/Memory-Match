package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

class GameStateMachineTest {
    private val testDispatchers =
        CoroutineDispatchers(
            main = Dispatchers.Default,
            mainImmediate = Dispatchers.Default,
            io = Dispatchers.Default,
            default = Dispatchers.Default,
        )

    @Test
    fun `initial state is correct`() {
        val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
        val machine =
            GameStateMachine(
                scope = CoroutineScope(Dispatchers.Default),
                dispatchers = testDispatchers,
                initialState = initialState,
                initialTimeSeconds = 60,
                onSaveState = { _, _ -> },
            )

        assertEquals(initialState, machine.state.value)
    }
}
