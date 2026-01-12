package io.github.smithjustinn.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ExplosionParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ExplosionEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    colors: List<Color> = listOf(Color.Yellow, Color.Red, Color.Cyan, Color.Magenta, Color.White)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val particles = remember {
        List(particleCount) {
            val angle = Random.nextFloat() * 2 * kotlin.math.PI
            val speed = Random.nextFloat() * 15f + 5f
            ExplosionParticle(
                x = 0f,
                y = 0f,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                color = colors.random(),
                size = Random.nextFloat() * 10f + 5f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        particles.forEach { particle ->
            val currentX = center.x + particle.vx * progress * 50f
            val currentY = center.y + particle.vy * progress * 50f
            val alpha = 1f - progress
            
            rotate(particle.rotation + particle.rotationSpeed * progress * 100f, Offset(currentX, currentY)) {
                drawRect(
                    color = particle.color.copy(alpha = alpha),
                    topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                    size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                )
            }
        }
    }
}
