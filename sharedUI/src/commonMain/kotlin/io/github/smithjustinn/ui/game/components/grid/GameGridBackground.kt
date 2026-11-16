package io.github.smithjustinn.ui.game.components.grid

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import io.github.smithjustinn.theme.PokerTheme
import kotlin.random.Random

private const val FELT_TEXTURE_SEED = 42
private const val FELT_TEXTURE_POINT_COUNT = 1000

private const val SPOTLIGHT_X_START = 0.2f
private const val SPOTLIGHT_X_END = 0.8f
private const val SPOTLIGHT_Y_OFFSET_FRACTION = 0.3f
private const val SPOTLIGHT_DURATION_MS = 15000
private const val SPOTLIGHT_ALPHA = 0.35f
private const val SPOTLIGHT_RADIUS_MULTIPLIER = 0.9f

private const val NOISE_ALPHA = 0.03f
private const val NOISE_STROKE_WIDTH = 2f

/**
 * Felt texture for the table: a slow drifting spotlight and a fine grain. It is transparent, so
 * the screen-wide felt gradient painted by GameBackground shows through and the HUD panels,
 * the cards and the table all sit on one continuous surface.
 */
@Composable
internal fun GridBackground(modifier: Modifier = Modifier) {
    val spotlightColor = PokerTheme.colors.feltGreenCenter

    // Dynamic Spotlight Animation
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight")
    // Bolt: Use State<Float> instead of delegated property to defer reads to draw phase
    val spotlightXState =
        infiniteTransition.animateFloat(
            initialValue = SPOTLIGHT_X_START,
            targetValue = SPOTLIGHT_X_END,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = SPOTLIGHT_DURATION_MS, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "spotlightX",
        )

    Box(
        modifier =
            modifier
                // Own layer: the spotlight redraws every frame, and without this it would make
                // the screen-wide felt and every sibling redraw with it.
                .graphicsLayer()
                .drawWithCache {
                    // Bolt: Generate noise points only when size changes, avoiding allocation on every frame
                    val random = Random(FELT_TEXTURE_SEED)
                    val points =
                        List(FELT_TEXTURE_POINT_COUNT) {
                            Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                        }
                    // Built once per size around x = 0 and slid sideways while drawing.
                    val spotlight =
                        Brush.radialGradient(
                            colors = listOf(spotlightColor.copy(alpha = SPOTLIGHT_ALPHA), Color.Transparent),
                            center = Offset(0f, size.height * SPOTLIGHT_Y_OFFSET_FRACTION),
                            radius = size.maxDimension * SPOTLIGHT_RADIUS_MULTIPLIER,
                        )

                    onDrawBehind {
                        // Bolt: Read state inside draw phase to skip recomposition
                        val shift = size.width * spotlightXState.value
                        translate(left = shift) {
                            drawRect(brush = spotlight, topLeft = Offset(-shift, 0f), size = size)
                        }
                        drawNoiseTexture(points)
                    }
                },
    )
}

private fun DrawScope.drawNoiseTexture(points: List<Offset>) {
    drawPoints(
        points = points,
        pointMode = PointMode.Points,
        color = Color.White.copy(alpha = NOISE_ALPHA),
        strokeWidth = NOISE_STROKE_WIDTH,
    )
}
