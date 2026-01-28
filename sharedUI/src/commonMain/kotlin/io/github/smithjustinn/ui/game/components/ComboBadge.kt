package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.combo_format
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ComboBadge(
    state: ComboBadgeState,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    AnimatedVisibility(
        visible = state.combo > 1,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        val comboPulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue =
                if (state.isHeatMode) {
                    if (compact) PULSE_SCALE_HEAT_COMPACT else PULSE_SCALE_HEAT
                } else {
                    if (compact) PULSE_SCALE_DEFAULT_COMPACT else PULSE_SCALE_DEFAULT
                },
            animationSpec =
                infiniteRepeatable(
                    animation = tween(COMBO_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
        )

        ComboBadgeContent(
            state = state,
            compact = compact,
            pulseScale = comboPulseScale,
        )
    }
}

@Composable
private fun ComboBadgeContent(
    state: ComboBadgeState,
    compact: Boolean,
    pulseScale: Float,
    modifier: Modifier = Modifier,
) {
    val colors = PokerTheme.colors
    val badgeColor =
        when {
            state.isHeatMode -> colors.tacticalRed
            state.isMegaBonus -> colors.goldenYellow
            else -> colors.goldenYellow
        }

    val chipSize = if (compact) 32.dp else 48.dp
    val stackOffset = if (compact) 2.dp else 4.dp
    val maxChips = 5
    // Show at most 5 chips in the stack, but at least 1 for the combo itself
    val chipCount = (state.combo - 1).coerceIn(1, maxChips)

    Box(
        modifier = modifier.scale(pulseScale),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Render the stack of chips
        repeat(chipCount) { index ->
            val yOffset = -stackOffset * index
            ChipVisual(
                color = badgeColor,
                chipSize = chipSize,
                modifier =
                    Modifier
                        .padding(bottom = yOffset.coerceAtLeast(0.dp))
                        .shadow(
                            elevation = (index + 1).dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.5f),
                            spotColor = Color.Black.copy(alpha = 0.5f),
                        ),
            )
        }

        // Top Chip with Text
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .padding(bottom = (stackOffset * (chipCount - 1)).coerceAtLeast(0.dp)),
        ) {
            ChipVisual(
                color = badgeColor,
                chipSize = chipSize,
                modifier =
                    Modifier.shadow(
                        elevation = (chipCount + 1).dp,
                        shape = CircleShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black,
                    ),
            )

            Text(
                text = "${state.combo}x",
                style =
                    PokerTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (compact) 10.sp else 14.sp,
                        letterSpacing = 0.sp,
                    ),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ChipVisual(
    color: Color,
    chipSize: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(chipSize)) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // 1. Base Chip Color
        drawCircle(
            color = color,
            center = center,
            radius = radius,
        )

        // 2. Dashed Ring (White spots) - Reusing logic from PokerChip.kt but simplified
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            center = center,
            radius = radius * 0.85f,
            style =
                Stroke(
                    width = radius * 0.15f,
                    pathEffect =
                        PathEffect.dashPathEffect(
                            intervals = floatArrayOf(20f, 20f),
                            phase = 0f,
                        ),
                ),
        )

        // 3. Inner Circle border
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            center = center,
            radius = radius * 0.6f,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

private const val COMBO_ANIMATION_DURATION_MS = 400
private const val PULSE_SCALE_HEAT_COMPACT = 1.08f
private const val PULSE_SCALE_HEAT = 1.15f
private const val PULSE_SCALE_DEFAULT_COMPACT = 1.05f
private const val PULSE_SCALE_DEFAULT = 1.1f
