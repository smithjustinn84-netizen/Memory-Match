package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.PokerTheme

private const val ELEVATION_PRESSED = 2
private const val ELEVATION_SELECTED = 12
private const val ELEVATION_DEFAULT = 4
private const val GLOW_ALPHA_SELECTED = 0f
private const val GLOW_RADIUS_FACTOR = 0.8f

@Composable
fun PokerChip(
    text: String,
    contentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    chipSize: Dp = 60.dp,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val chipAnimations = rememberPokerChipAnimations(isSelected, interactionSource)
    val elevation = chipAnimations.elevation
    val scale = chipAnimations.scale
    val glowAlpha = chipAnimations.glowAlpha

    val glowColor = PokerTheme.colors.goldenYellow

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        PokerChipContent(
            chipSize = chipSize,
            scale = scale,
            glowAlpha = glowAlpha,
            glowColor = glowColor,
            elevation = elevation,
            interactionSource = interactionSource,
            onClick = onClick,
            contentColor = contentColor,
            text = text,
            isSelected = isSelected,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun PokerChipContent(
    chipSize: Dp,
    scale: Float,
    glowAlpha: Float,
    glowColor: Color,
    elevation: Dp,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    contentColor: Color,
    text: String,
    isSelected: Boolean,
    contentDescription: String?,
) {
    val hapticsService = LocalAppGraph.current.hapticsService

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(chipSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.drawBehind {
                    if (glowAlpha > 0f) {
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                                    center = center,
                                    radius = size.minDimension * GLOW_RADIUS_FACTOR,
                                ),
                            radius = size.minDimension * GLOW_RADIUS_FACTOR,
                        )
                    }
                }.shadow(elevation, CircleShape)
                .semantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                }.selectable(
                    selected = isSelected,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
                        onClick()
                    },
                    role = Role.RadioButton,
                ),
    ) {
        ChipFace(
            contentColor = contentColor,
            modifier = Modifier.matchParentSize(),
        )

        ChipValueText(text = text)
    }
}

@Composable
private fun ChipValueText(text: String) {
    Text(
        text = text,
        style =
            PokerTheme.typography.titleMedium.copy(
                fontFamily = PokerTheme.fonts.mono,
                fontSize = CHIP_VALUE_FONT_SP.sp,
                fontWeight = FontWeight.Bold,
            ),
        color = PokerTheme.colors.cardStock,
    )
}

private const val CHIP_VALUE_FONT_SP = 17

@Composable
private fun rememberPokerChipAnimations(
    isSelected: Boolean,
    interactionSource: MutableInteractionSource,
): PokerChipAnimations {
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue =
            when {
                isPressed -> ELEVATION_PRESSED.dp
                isSelected -> ELEVATION_SELECTED.dp
                else -> ELEVATION_DEFAULT.dp
            },
        label = "chipElevation",
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) SCALE_SELECTED else SCALE_DEFAULT,
        label = "chipScale",
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) GLOW_ALPHA_SELECTED else 0f,
        label = "chipGlowAlpha",
    )

    return remember(elevation, scale, glowAlpha) {
        PokerChipAnimations(elevation, scale, glowAlpha)
    }
}

private data class PokerChipAnimations(
    val elevation: Dp,
    val scale: Float,
    val glowAlpha: Float,
)

@Composable
private fun ChipFace(
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val stock = PokerTheme.colors.cardStock
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // 1. Striped edge: alternating card-stock and chip-colour wedges
        drawCircle(color = contentColor, center = center, radius = radius)
        val sweep = FULL_CIRCLE_DEGREES / EDGE_WEDGE_COUNT
        repeat(EDGE_WEDGE_COUNT / 2) { index ->
            drawArc(
                color = stock,
                startAngle = EDGE_START_DEGREES + index * sweep * 2,
                sweepAngle = sweep,
                useCenter = true,
            )
        }

        // 2. Inner disc
        val innerRadius = radius * INNER_DISC_RADIUS_FACTOR
        drawCircle(color = contentColor, center = center, radius = innerRadius)

        // 3. Dashed inlay ring
        val dash = INLAY_DASH_DP.dp.toPx()
        drawCircle(
            color = stock.copy(alpha = INLAY_ALPHA),
            center = center,
            radius = innerRadius - INLAY_INSET_DP.dp.toPx(),
            style =
                Stroke(
                    width = INLAY_WIDTH_DP.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash), 0f),
                ),
        )
    }
}

private const val FULL_CIRCLE_DEGREES = 360f
private const val EDGE_WEDGE_COUNT = 16
private const val EDGE_START_DEGREES = -101.25f
private const val INNER_DISC_RADIUS_FACTOR = 0.74f
private const val INLAY_DASH_DP = 3f
private const val INLAY_INSET_DP = 1.5f
private const val INLAY_WIDTH_DP = 1.5f
private const val INLAY_ALPHA = 0.7f

private const val SCALE_SELECTED = 1.0f
private const val SCALE_DEFAULT = 1.0f
