package io.github.smithjustinn.ui.assets

import androidx.compose.ui.graphics.Color
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.theme.Ink
import io.github.smithjustinn.theme.OxbloodBack
import io.github.smithjustinn.ui.game.components.cards.CardBacks

/**
 * Returns the primary color associated with this [CardBackTheme].
 * These colors match the ones used in [CardBacks].
 */
fun CardBackTheme.getPreferredColor(): Color =
    when (this) {
        CardBackTheme.GEOMETRIC -> GeometricBlue
        CardBackTheme.CLASSIC -> OxbloodBack
        CardBackTheme.PATTERN -> Ink
        CardBackTheme.POKER -> OxbloodBack
    }

/**
 * Converts a hex string to a Compose [Color].
 * Supports formats: "#RRGGBB", "RRGGBB", "#AARRGGBB", "AARRGGBB".
 */
fun String.toColor(): Color {
    val hex = this.removePrefix("#")
    return try {
        when (hex.length) {
            6 -> Color(hex.toLong(16) or 0xFF000000L)
            8 -> Color(hex.toLong(16))
            else -> Color.Gray
        }
    } catch (e: NumberFormatException) {
        Color.Gray
    }
}

private val GeometricBlue = Color(0xFF1C3F6E)
