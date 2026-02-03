package io.github.smithjustinn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.assets.getPreferredColor

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = Color.Black.copy(alpha = 0.3f), // Recessed felt look
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing
    val shapes = PokerTheme.shapes
    val cardTheme = PokerTheme.cardTheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.medium,
        color = backgroundColor,
        border =
            border ?: BorderStroke(
                width = 1.dp,
                color = cardTheme.back.getPreferredColor().copy(alpha = 0.4f), // Brass-like inlay border
            ),
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            if (title != null) {
                Text(
                    text = title.uppercase(),
                    style = PokerTheme.typography.labelLarge,
                    color = colors.goldenYellow,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = spacing.medium),
                )
            }
            content()
        }
    }
}
