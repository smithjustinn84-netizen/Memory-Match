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
import kotlin.random.Random

private class SteamParticle(
    val size: Float,
    val speed: Float,
) {
    var x = 0f
    var y = 0f
    var alpha = 0f
    var life = 0f
    var maxLife = 0f
    
    fun reset(width: Float, height: Float) {
        x = Random.nextFloat() * width
        y = height + Random.nextFloat() * 100f // Start below
        life = 0f
        maxLife = Random.nextFloat() * 60f + 30f // Short burst
        alpha = Random.nextFloat() * 0.4f + 0.2f
    }
    
    fun update() {
        y -= speed
        life += 1f
        alpha -= 0.015f // Fade out fast
    }
}

@Composable
fun SteamEffect(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
) {
    if (!isVisible) return

    val particles = remember { 
        List(particleCount) {
             SteamParticle(
                size = Random.nextFloat() * 20f + 10f, // Large puffs
                speed = Random.nextFloat() * 8f + 4f   // Fast rising
            )
        }
    }
    
    val frameState = remember { mutableLongStateOf(0L) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Initial burst setup
            // We rely on the Canvas draw loop to init positions based on size
            while (true) {
                withFrameNanos { time ->
                    frameState.longValue = time
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val frame = frameState.longValue // Trigger redraw
        
        particles.forEach { p ->
             if (p.life == 0f && p.y == 0f) {
                 p.reset(size.width, size.height)
             }
             
             if (p.alpha > 0f) {
                 p.update()
                 
                 drawCircle(
                     color = Color.White.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                     radius = p.size,
                     center = Offset(p.x, p.y)
                 )
             }
        }
    }
}
