package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.PokerTheme

@Composable
fun WoodenDashboard(
    modifier: Modifier = Modifier,
    isHeatMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, // Higher min opacity
        targetValue = 1.0f, // Max opacity
        animationSpec =
            infiniteRepeatable(
                animation = tween(800, easing = LinearEasing), // Faster pulse
                repeatMode = RepeatMode.Reverse,
            ),
    )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.oakWood)
                .drawBehind {
                    // Heat Mode Glow
                    if (isHeatMode) {
                        drawRect(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            colors.tacticalRed.copy(alpha = glowAlpha),
                                            colors.tacticalRed.copy(alpha = 0.2f),
                                            Color.Transparent,
                                        ),
                                    startY = 0f,
                                    endY = size.height * 0.8f, // Covers more height
                                ),
                            blendMode = BlendMode.Screen, // Screen allows for a nice additive glow effect
                        )

                        // Extra bottom rim glow
                        drawLine(
                            brush =
                                Brush.horizontalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            colors.goldenYellow.copy(alpha = glowAlpha * 0.8f),
                                            Color.Transparent,
                                        ),
                                ),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 4.dp.toPx(),
                        )
                    }

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
