package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemoryGameLogicTest {

    @Test
    fun testDoubleDownActivation() {
        val config = ScoringConfig(heatModeThreshold = 3)
        // Ensure 3 pairs are available (min requirement)
        var state = MemoryGameLogic.createInitialState(pairCount = 3, config = config)
        
        // Simulating Heat Mode (Combo 3)
        state = state.copy(comboMultiplier = 3)
        
        // Activate Double Down
        val doubleDownState = MemoryGameLogic.activateDoubleDown(state)
        assertTrue(doubleDownState.isDoubleDownActive, "Double Down should be active given adequate combo and pairs")
    }
}
