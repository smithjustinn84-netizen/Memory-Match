package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import io.github.smithjustinn.theme.Brass
import io.github.smithjustinn.theme.Bronze
import io.github.smithjustinn.theme.ModernGold

// Increased duration for a slower, more subtle effect
private const val SHIMMER_DURATION_MS = 4000
private const val SHIMMER_MAX_OFFSET = 2000f
private const val SHIMMER_START_OFFSET = 1000f

private val GlimmerColors =
    listOf(
        Bronze,
        ModernGold,
        Bronze,
        Brass,
        Bronze,
        ModernGold,
        Bronze,
        Brass,
        Bronze,
    )

/** How far the glimmer gradient has travelled, in pixels. Read it while drawing, not in composition. */
@Composable
fun rememberGlimmerTranslation(): State<Float> =
    rememberInfiniteTransition(label = "glimmer").animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_MAX_OFFSET,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "glimmer_translate",
    )

private fun glimmerBrush(translation: Float): Brush =
    Brush.linearGradient(
        colors = GlimmerColors,
        start = Offset(translation - SHIMMER_START_OFFSET, translation - SHIMMER_START_OFFSET),
        end = Offset(translation, translation),
        tileMode = TileMode.Clamp,
    )

/**
 * A brush that changes every frame, so whatever reads it recomposes every frame. Prefer
 * [drawGlimmerOverContent] for anything that stays on screen.
 */
@Composable
fun rememberGlimmerBrush(): Brush = glimmerBrush(rememberGlimmerTranslation().value)

/**
 * Paints the moving glimmer gradient over whatever the content drew, keeping the content's shape.
 * The node needs an offscreen layer (CompositingStrategy.Offscreen) so the gradient is masked by
 * the content alone. The gradient is built once per size; only the draw runs each frame.
 */
fun CacheDrawScope.drawGlimmerOverContent(translation: State<Float>): DrawResult {
    val brush = glimmerBrush(0f)
    return onDrawWithContent {
        drawContent()
        val shift = translation.value
        translate(left = shift, top = shift) {
            drawRect(brush = brush, topLeft = Offset(-shift, -shift), size = size, blendMode = BlendMode.SrcIn)
        }
    }
}
