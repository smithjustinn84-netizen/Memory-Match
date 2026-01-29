package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.smithjustinn.theme.PokerTheme
import kotlin.random.Random

/**
 * A particle representing a single ember.
 */
private class EmberParticle(
    val color: Color,
    val size: Float,
    val speed: Float,
) {
    var x = 0f
    var y = 0f
    var alpha = 0f
    var phase = 0f // For alpha oscillation or lifecycle
    var drift = 0f // Horizontal drift

    // Lifecycle state
    var life = 0f
    var maxLife = 0f

    fun reset(
        width: Float,
        height: Float,
    ) {
        x = Random.nextFloat() * width
        y = height + size // Start just below screen
        life = 0f
        // Significantly increased life to allow reaching top of screen
        // At speed ~3px/frame and 60fps, 300 frames = 900px rise.
        // Need more for full screen (approx 2000px height on devices)
        maxLife = Random.nextFloat() * 200f + 300f
        alpha = 0f
        drift = (Random.nextFloat() - 0.5f) * 0.5f
    }

    fun update() {
        y -= speed
        x += drift
        life += 1f

        // simple fade in/out
        val halfLife = maxLife / 2
        alpha = if (life < halfLife) {
            (life / halfLife).coerceIn(0f, 1f)
        } else {
            (1f - (life - halfLife) / halfLife).coerceIn(0f, 1f)
        } * 0.8f // Max alpha
    }
}

@Composable
fun ParticleEmbers(
    isHeatMode: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 80, // Doubled count
) {
    val colors =
        listOf(
            PokerTheme.colors.tacticalRed,
            PokerTheme.colors.goldenYellow,
            Color(0xFFFF5722), // Deep Orange
            Color(0xFFFFC107), // Amber
        )

    val particles =
        remember {
            List(particleCount) {
                EmberParticle(
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 4f, // Larger size (4-16dp)
                    speed = Random.nextFloat() * 3f + 1f, // Faster speed
                )
            }
        }
    // Set initial off-screen positions? or let them spawn naturally.
    // We'll handle canvas size initialization in the drawing phase or init.

    val frameState = remember { mutableLongStateOf(0L) }

    LaunchedEffect(isHeatMode) {
        if (isHeatMode) {
            while (true) {
                withFrameNanos { time ->
                    frameState.longValue = time
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Trigger update on frame
        if (isHeatMode) {
            val frame = frameState.longValue

            particles.forEach { p ->
                // Initialize if needed (first run or recycled)
                if (p.y < -50f || p.life >= p.maxLife || (p.x == 0f && p.y == 0f)) {
                    // Only respawn if heat mode is active
                    p.reset(size.width, size.height)
                    // If purely initializing, randomize Y so they don't all start at bottom
                    if (p.x == 0f && p.y == 0f) {
                        p.y = Random.nextFloat() * size.height
                    }
                }

                p.update()

                // Add a glow effect by drawing a larger, softer circle behind
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * 0.3f),
                    radius = p.size, // Double radius for glow
                    center = Offset(p.x, p.y),
                )

                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size / 2,
                    center = Offset(p.x, p.y),
                )
            }
        }
    }
}
