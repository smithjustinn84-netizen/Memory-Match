package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.theme.ModernGold
import io.github.smithjustinn.theme.OxbloodBack
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.assets.getPreferredColor
import io.github.smithjustinn.ui.assets.toColor
import kotlin.math.abs

// Animation durations (milliseconds)
private const val SHIMMER_ANIMATION_DURATION_MS = 1500
private const val SHIMMER_TRANSLATE_TARGET = 2000f
private const val SHIMMER_OFFSET = 500f

// Shared anatomy of every back: card-stock border, fine pattern, gold diamond emblem
private const val HATCH_STEP_DP = 8
private const val HATCH_ALPHA = 0.16f
private const val HEAT_HATCH_ALPHA = 0.22f
private const val CHECK_STEP_DP = 8
private const val CHECK_ALPHA = 0.14f
private const val DOT_STEP_DP = 12
private const val DOT_RADIUS_DP = 1.5f
private const val DOT_ALPHA = 0.7f
private const val CELL_CENTER = 0.5f
private const val STRIPE_WIDTH_DP = 8
private const val STRIPE_SHADE = 0.25f
private const val EMBLEM_WIDTH_FRACTION = 0.29f
private const val EMBLEM_CORE_FRACTION = 0.25f
private const val EMBLEM_STROKE_FRACTION = 0.085f
private const val SQRT_2 = 1.4142135f

private val CardBackShape = RoundedCornerShape(CARD_CORNER_RADIUS_DP.dp)

object CardBacks {
    @Composable
    fun Standard(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { GeometricCardBack(Color(0xFF1A237E)) } // Deep Blue

    @Composable
    fun Dark(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { GeometricCardBack(Color(0xFF263238)) } // Charcoal

    @Composable
    fun Nature(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { GeometricCardBack(Color(0xFF1B5E20)) } // Forest Green

    @Composable
    fun Classic(modifier: Modifier = Modifier) = PreviewContainer(modifier) { ClassicCardBack(OxbloodBack) }

    @Composable
    fun Pattern(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { PatternCardBack(Color(0xFF4527A0)) } // Deep Purple

    @Composable
    fun Poker(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { PokerCardBack(Color(0xFF004D40)) } // Deep Teal

    @Composable
    fun Render(
        theme: CardTheme,
        modifier: Modifier = Modifier,
        backgroundColor: Color = theme.backColorHex?.toColor() ?: theme.back.getPreferredColor(),
        rotation: Float = 0f,
    ) {
        val rotationState = rememberUpdatedState(rotation)
        Box(modifier = modifier) {
            CardBack(
                theme = theme.back,
                backColor = backgroundColor,
                rotation = rotationState,
            )
        }
    }

    @Composable
    private fun PreviewContainer(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .clip(CardBackShape)
                    .background(PokerTheme.colors.cardStock),
        ) {
            content()
        }
    }
}

@Composable
@Suppress("ktlint:compose:state-param-check")
internal fun CardBack(
    theme: CardBackTheme,
    backColor: Color,
    rotation: State<Float>,
    isHeatMode: Boolean = false,
) {
    val rimLightBrush =
        remember {
            Brush.horizontalGradient(
                colors =
                    listOf(
                        Color.Transparent,
                        Color.White,
                        Color.Transparent,
                    ),
            )
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationY = FULL_ROTATION }
                .drawWithContent {
                    drawContent()

                    val currentRotation = rotation.value
                    val rimLightAlpha = (1f - abs(currentRotation - HALF_ROTATION) / HALF_ROTATION).coerceIn(0f, 1f)

                    if (rimLightAlpha > 0f) {
                        drawRect(
                            brush = rimLightBrush,
                            alpha = rimLightAlpha * HIGH_ALPHA,
                        )
                    }
                },
    ) {
        when (theme) {
            CardBackTheme.GEOMETRIC -> GeometricCardBack(backColor, isHeatMode)
            CardBackTheme.CLASSIC -> ClassicCardBack(backColor, isHeatMode)
            CardBackTheme.PATTERN -> PatternCardBack(backColor)
            CardBackTheme.POKER -> PokerCardBack(backColor)
        }
    }
}

/**
 * A diagonal gold sheen sweeping across a card back. The gradient is built once per size and the
 * animation is read only while drawing, on its own layer, so a sweep never recomposes anything
 * and never makes the card underneath redraw.
 */
@Composable
internal fun ShimmerEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val translateState =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = SHIMMER_TRANSLATE_TARGET,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(SHIMMER_ANIMATION_DURATION_MS, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer()
                .drawWithCache {
                    val brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.0f),
                                    ModernGold.copy(alpha = MEDIUM_ALPHA),
                                    Color.White.copy(alpha = 0.0f),
                                ),
                            start = Offset(-SHIMMER_OFFSET, -SHIMMER_OFFSET),
                            end = Offset.Zero,
                        )
                    onDrawBehind {
                        val shift = translateState.value
                        translate(left = shift, top = shift) {
                            drawRect(brush = brush, topLeft = Offset(-shift, -shift), size = size)
                        }
                    }
                },
    )
}

/** Colours every back shares; read once per composition so heat mode re-tints them for free. */
private class CardBackPalette(
    val border: Color,
    val pattern: Color,
    val emblem: Color,
)

@Composable
private fun rememberCardBackPalette(
    patternAlpha: Float,
    isHeatMode: Boolean = false,
): CardBackPalette {
    val colors = PokerTheme.colors
    val pattern =
        if (isHeatMode) {
            colors.goldenYellow.copy(alpha = HEAT_HATCH_ALPHA)
        } else {
            colors.cardStock.copy(alpha = patternAlpha)
        }
    return remember(colors, pattern) {
        CardBackPalette(border = colors.cardStock, pattern = pattern, emblem = colors.goldenYellow)
    }
}

/** The small diamond-in-a-diamond mark at the centre of every back. */
private class CardBackEmblem(
    val outer: Path,
    val core: Path,
    val stroke: Stroke,
)

private fun createEmblem(size: Size): CardBackEmblem {
    val side = size.width * EMBLEM_WIDTH_FRACTION
    val outerRadius = side / SQRT_2
    val coreRadius = side * EMBLEM_CORE_FRACTION / SQRT_2
    val centerX = size.width / 2
    val centerY = size.height / 2
    return CardBackEmblem(
        outer = createDiamondPath(centerX, centerY, outerRadius),
        core = createDiamondPath(centerX, centerY, coreRadius),
        stroke = Stroke(width = side * EMBLEM_STROKE_FRACTION),
    )
}

private fun createDiamondPath(
    centerX: Float,
    centerY: Float,
    radius: Float,
): Path =
    Path().apply {
        moveTo(centerX, centerY - radius)
        lineTo(centerX + radius, centerY)
        lineTo(centerX, centerY + radius)
        lineTo(centerX - radius, centerY)
        close()
    }

/**
 * Parallel 45-degree lines [step] apart (measured across the lines), in one or both directions.
 */
private fun createDiagonalPath(
    size: Size,
    step: Float,
    crossed: Boolean,
): Path {
    val path = Path()
    val axisStep = step * SQRT_2
    val count = ((size.width + size.height) / axisStep).toInt() + 1
    for (i in 0..count) {
        val offset = i * axisStep
        path.moveTo(offset, 0f)
        path.lineTo(offset - size.height, size.height)
        if (crossed) {
            path.moveTo(offset - size.height, 0f)
            path.lineTo(offset, size.height)
        }
    }
    return path
}

/** Emblem and card-stock border, drawn over the pattern. The clip trims the border's outer half. */
private fun DrawScope.drawBackFrame(
    palette: CardBackPalette,
    emblem: CardBackEmblem,
    coreColor: Color,
) {
    drawPath(emblem.outer, color = coreColor)
    drawPath(emblem.outer, color = palette.emblem, style = emblem.stroke)
    drawPath(emblem.core, color = palette.emblem)

    drawRoundRect(
        color = palette.border,
        cornerRadius = CornerRadius(CARD_CORNER_RADIUS_DP.dp.toPx()),
        style = Stroke(width = CARD_BORDER_WIDTH_DP.dp.toPx() * 2),
    )
}

/** Classic: fine cross-hatch. The default back, and the one the heat deck uses. */
@Composable
internal fun ClassicCardBack(
    baseColor: Color,
    isHeatMode: Boolean = false,
) {
    val palette = rememberCardBackPalette(HATCH_ALPHA, isHeatMode)
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(CardBackShape)
                .drawWithCache {
                    val hatch = createDiagonalPath(size, HATCH_STEP_DP.dp.toPx(), crossed = true)
                    val hatchStroke = Stroke(width = 1.dp.toPx())
                    val emblem = createEmblem(size)

                    onDrawBehind {
                        drawRect(baseColor)
                        drawPath(hatch, color = palette.pattern, style = hatchStroke)
                        drawBackFrame(palette, emblem, coreColor = baseColor)
                    }
                },
    )
}

/** Geometric: a soft checkerboard. */
@Composable
internal fun GeometricCardBack(
    baseColor: Color,
    isHeatMode: Boolean = false,
) {
    val palette = rememberCardBackPalette(CHECK_ALPHA, isHeatMode)
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(CardBackShape)
                .drawWithCache {
                    val checks = createCheckPath(size, CHECK_STEP_DP.dp.toPx())
                    val emblem = createEmblem(size)

                    onDrawBehind {
                        drawRect(baseColor)
                        drawPath(checks, color = palette.pattern)
                        drawBackFrame(palette, emblem, coreColor = baseColor)
                    }
                },
    )
}

private fun createCheckPath(
    size: Size,
    step: Float,
): Path {
    val path = Path()
    val columns = (size.width / step).toInt() + 1
    val rows = (size.height / step).toInt() + 1
    for (x in 0 until columns) {
        for (y in 0 until rows) {
            if ((x + y) % HALF_DIVISOR == 0) {
                path.addRect(Rect(offset = Offset(x * step, y * step), size = Size(step, step)))
            }
        }
    }
    return path
}

/** Pattern: a grid of small gold dots. */
@Composable
internal fun PatternCardBack(baseColor: Color) {
    val palette = rememberCardBackPalette(patternAlpha = 0f)
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(CardBackShape)
                .drawWithCache {
                    val dots = createDotPath(size, DOT_STEP_DP.dp.toPx(), DOT_RADIUS_DP.dp.toPx())
                    val dotColor = palette.emblem.copy(alpha = DOT_ALPHA)
                    val emblem = createEmblem(size)

                    onDrawBehind {
                        drawRect(baseColor)
                        drawPath(dots, color = dotColor)
                        drawBackFrame(palette, emblem, coreColor = baseColor)
                    }
                },
    )
}

private fun createDotPath(
    size: Size,
    step: Float,
    radius: Float,
): Path {
    val path = Path()
    val columns = (size.width / step).toInt() + 1
    val rows = (size.height / step).toInt() + 1
    for (x in 0 until columns) {
        for (y in 0 until rows) {
            val center = Offset((x + CELL_CENTER) * step, (y + CELL_CENTER) * step)
            path.addOval(Rect(center = center, radius = radius))
        }
    }
    return path
}

/** Poker: bold diagonal stripes in two shades of the back colour. */
@Composable
internal fun PokerCardBack(baseColor: Color) {
    val palette = rememberCardBackPalette(patternAlpha = 0f)
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(CardBackShape)
                .drawWithCache {
                    val stripeWidth = STRIPE_WIDTH_DP.dp.toPx()
                    val stripes = createDiagonalPath(size, stripeWidth * 2, crossed = false)
                    val stripeStroke = Stroke(width = stripeWidth)
                    val shade = lerp(baseColor, Color.Black, STRIPE_SHADE)
                    val emblem = createEmblem(size)

                    onDrawBehind {
                        drawRect(baseColor)
                        drawPath(stripes, color = shade, style = stripeStroke)
                        drawBackFrame(palette, emblem, coreColor = shade)
                    }
                },
    )
}
