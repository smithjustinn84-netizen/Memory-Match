package io.github.smithjustinn.domain.models

enum class Suit(val symbol: String) {
    Hearts("♥"),
    Diamonds("♦"),
    Clubs("♣"),
    Spades("♠");

    val isRed: Boolean get() = this == Hearts || this == Diamonds
}

enum class Rank(val symbol: String) {
    Ace("A"),
    Two("2"),
    Three("3"),
    Four("4"),
    Five("5"),
    Six("6"),
    Seven("7"),
    Eight("8"),
    Nine("9"),
    Ten("10"),
    Jack("J"),
    Queen("Q"),
    King("K")
}

data class CardState(
    val id: Int,
    val suit: Suit,
    val rank: Rank,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val isError: Boolean = false
)
