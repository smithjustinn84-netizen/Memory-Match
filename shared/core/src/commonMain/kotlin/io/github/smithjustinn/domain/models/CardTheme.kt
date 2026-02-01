package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class CardBackTheme {
    GEOMETRIC,
    CLASSIC,
    PATTERN,
    POKER,
}

@Serializable
enum class CardSymbolTheme {
    CLASSIC,
    MINIMAL,
    TEXT_ONLY,
    POKER,
}

@Serializable
data class CardDisplaySettings(
    val backTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val symbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val areSuitsMultiColored: Boolean = false,
)
