package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.*
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HighRollerLogicTest {
    private val config = ScoringConfig(theNutsThreshold = 3)

    @Test
    fun `match should increment currentPot with stage multiplier`() {
        // Arrange
        val stage = CircuitStage.QUALIFIER // Multiplier 2, Penalty 0.2
        val state =
            MemoryGameState(
                mode = GameMode.HIGH_ROLLER,
                circuitStage = stage,
                config = config,
                cards = (createMatchedPair(0, Rank.Ace) + createMatchedPair(2, Rank.King)).toImmutableList(),
            )

        val card1 = state.cards[0]
        val card2 = state.cards[1]

        // Act
        val (flippedState, _) = MemoryGameLogic.flipCard(state, card1.id)
        val (finalState, event) = MemoryGameLogic.flipCard(flippedState, card2.id)

        // Assert
        val basePoints = config.baseMatchPoints // 100
        val expectedPoints = basePoints // 100 (Combo 0 for first match)
        // Qualifier multiplier is 1
        val expectedPot = expectedPoints * stage.potGrowthMultiplier

        assertEquals(expectedPot, finalState.currentPot, "Pot should grow with multiplier")
        assertEquals(0, finalState.bankedScore, "Score should not be banked yet")
    }

    @Test
    fun `reaching The Nuts should bank the pot`() {
        // Arrange: Start with a pot and combo multiplier at threshold - 1
        val stage = CircuitStage.QUALIFIER
        val state =
            MemoryGameState(
                mode = GameMode.HIGH_ROLLER,
                circuitStage = stage,
                currentPot = 500,
                comboMultiplier = 2, // Threshold is 3
                config = config,
                pairCount = 10,
                cards = createMatchedPair(0, Rank.Ace).toImmutableList(),
            )

        val card1 = state.cards[0]
        val card2 = state.cards[1]

        // Act
        val (flippedState, _) = MemoryGameLogic.flipCard(state, card1.id)
        val (finalState, event) = MemoryGameLogic.flipCard(flippedState, card2.id)

        // Assert
        assertTrue(finalState.comboMultiplier >= config.theNutsThreshold)
        assertEquals(finalState.currentPot, 0, "Pot should be emptied into banked score")
        assertTrue(finalState.bankedScore > 500, "Banked score should include old pot + new match points")
    }

    @Test
    fun `clearing board should bank the pot`() {
        // Arrange: Last pair matching
        val stage = CircuitStage.QUALIFIER
        val state =
            MemoryGameState(
                mode = GameMode.HIGH_ROLLER,
                circuitStage = stage,
                currentPot = 1000,
                pairCount = 1,
                config = config,
                cards = createMatchedPair(0, Rank.Ace).toImmutableList(),
            )

        // Act
        val (flippedState, _) = MemoryGameLogic.flipCard(state, state.cards[0].id)
        val (finalState, _) = MemoryGameLogic.flipCard(flippedState, state.cards[1].id)

        // Assert
        assertTrue(finalState.isGameWon)
        assertEquals(0, finalState.currentPot)
        assertTrue(finalState.bankedScore >= 1000)
    }

    @Test
    fun `mismatch should apply bad beat penalty`() {
        // Arrange
        val stage = CircuitStage.QUALIFIER // Penalty 0.2
        val state =
            MemoryGameState(
                mode = GameMode.HIGH_ROLLER,
                circuitStage = stage,
                currentPot = 1000,
                config = config,
                cards =
                    listOf(
                        CardState(0, Suit.Hearts, Rank.Ace),
                        CardState(1, Suit.Spades, Rank.King),
                    ).toImmutableList(),
            )

        // Act
        val (flippedState, _) = MemoryGameLogic.flipCard(state, 0)
        val (finalState, _) = MemoryGameLogic.flipCard(flippedState, 1)

        // Assert
        val expectedPot = (1_000 * (1 - stage.bustPenalty)).toInt() // 800
        assertEquals(expectedPot, finalState.currentPot, "Pot should be reduced by bad beat penalty")
    }

    @Test
    fun `pot reaching zero with zero banked should result in Busted`() {
        // Arrange
        val stage = CircuitStage.GRAND_FINALE // Penalty 0.5
        val state =
            MemoryGameState(
                mode = GameMode.HIGH_ROLLER,
                circuitStage = stage,
                currentPot = 0, // Start with 0 to ensure bust
                bankedScore = 0,
                config = config,
                cards =
                    listOf(
                        CardState(0, Suit.Hearts, Rank.Ace),
                        CardState(1, Suit.Spades, Rank.King),
                    ).toImmutableList(),
            )

        // Act
        val (flippedState, _) = MemoryGameLogic.flipCard(state, 0)
        val (finalState, _) = MemoryGameLogic.flipCard(flippedState, 1)

        // Assert
        assertTrue(finalState.isBusted, "Player should be busted")
        assertTrue(finalState.isGameOver, "Game should be over on bust")
    }

    private fun createMatchedPair(
        startId: Int,
        rank: Rank,
    ): List<CardState> =
        listOf(
            CardState(startId, Suit.Hearts, rank),
            CardState(startId + 1, Suit.Hearts, rank), // Must match Suit AND Rank
        )
}
