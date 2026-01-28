package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.PokerTheme

@Composable
fun WoodenDashboard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.oakWood)
                .drawBehind {
                    // Outer Bevel (Bottom Highlight)
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx(),
                    )

                    // Inner Shadow (Top)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.3f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 4.dp.toPx(),
                    )

                    // Grain accents (Subtle horizontal lines)
                    val grainColor = Color.Black.copy(alpha = 0.05f)
                    val grainWidth = 1.dp.toPx()
                    drawLine(
                        grainColor,
                        Offset(0f, size.height * 0.3f),
                        Offset(size.width, size.height * 0.3f),
                        grainWidth,
                    )
                    drawLine(
                        grainColor,
                        Offset(0f, size.height * 0.7f),
                        Offset(size.width, size.height * 0.7f),
                        grainWidth,
                    )
                }.padding(vertical = spacing.small),
    ) {
        content()
    }
}
