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
import androidx.compose.ui.unit.em
import io.github.smithjustinn.theme.PokerTheme

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = PokerTheme.colors.raisedSurface,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing
    val shapes = PokerTheme.shapes

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.large,
        color = backgroundColor,
        border =
            border ?: BorderStroke(width = 1.dp, color = colors.goldenYellow.copy(alpha = CARD_BORDER_ALPHA)),
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            if (title != null) {
                Text(
                    text = title.uppercase(),
                    style = PokerTheme.typography.labelMedium.copy(letterSpacing = TITLE_LETTER_SPACING_EM.em),
                    color = colors.goldenYellow,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = spacing.medium),
                )
            }
            content()
        }
    }
}

private const val CARD_BORDER_ALPHA = 0.22f
private const val TITLE_LETTER_SPACING_EM = 0.08f
