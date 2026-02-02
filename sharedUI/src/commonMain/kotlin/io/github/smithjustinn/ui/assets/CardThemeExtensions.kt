package io.github.smithjustinn.ui.assets

import androidx.compose.ui.graphics.Color
import io.github.smithjustinn.domain.models.CardBackTheme

/**
 * Returns the primary color associated with this [CardBackTheme].
 * These colors match the ones used in [io.github.smithjustinn.ui.game.components.cards.CardBacks].
 */
fun CardBackTheme.getPreferredColor(): Color =
    when (this) {
        CardBackTheme.GEOMETRIC -> Color(0xFF1A237E) // Deep Blue (Standard / Geometric)
        CardBackTheme.CLASSIC -> Color(0xFFB71C1C) // Deep Red
        CardBackTheme.PATTERN -> Color(0xFF4527A0) // Deep Purple
        CardBackTheme.POKER -> Color(0xFF004D40) // Deep Teal
    }
