package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

data class FlyingPoint(
    val id: Long,
    val startPos: Offset,
    val targetPos: Offset,
    val startTime: kotlin.time.TimeMark,
    val duration: kotlin.time.Duration,
    val color: Color,
    val size: Float,
    val delay: kotlin.time.Duration,
)

@Composable
fun ScoreFlyingEffect(
    matchPositions: List<Offset>,
    targetPosition: Offset,
    modifier: Modifier = Modifier,
) {
    var points by remember { mutableStateOf(listListOf<FlyingPoint>()) }
    val timeSource = TimeSource.Monotonic

    // Trigger points when matchPositions changes and is not empty
    LaunchedEffect(matchPositions) {
        if (matchPositions.isNotEmpty()) {
            val now = timeSource.markNow()
            val newPoints =
                matchPositions.flatMap { pos ->
                    List(12) { i ->
                        // Increased count
                        FlyingPoint(
                            id = Random.nextLong(),
                            startPos = pos,
                            targetPos = targetPosition,
                            startTime = now,
                            duration = (600 + Random.nextInt(600)).milliseconds, // Slightly faster/varied
                            color =
                                listOf(
                                    Color(0xFFFFD700), // Gold
                                    Color(0xFFB5A642), // Brass
                                    Color.White,
                                    Color(0xFFFDE68A), // Pale Gold
                                ).random(),
                            size = 8f + Random.nextFloat() * 8f, // Increased size
                            delay = (i * 30 + Random.nextInt(150)).milliseconds,
                        )
                    }
                }
            points = points + newPoints
        }
    }

    // Frame ticker to ensure smooth animation redraws
    var frameTick by remember { mutableStateOf(0L) }
    if (points.isNotEmpty()) {
        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos { frameTick = it }
            }
        }
    }

    // Cleanup finished points
    SideEffect {
        val remaining =
            points.filter { point ->
                point.startTime.elapsedNow() < point.duration + point.delay
            }
        if (remaining.size != points.size) {
            points = remaining
        }
    }

    if (points.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        frameTick // trigger redraw

        points.forEach { point ->
            val elapsedSinceStart = point.startTime.elapsedNow()
            val activeTime = elapsedSinceStart - point.delay

            if (activeTime.isPositive()) {
                val progress = (activeTime / point.duration).coerceIn(0.0, 1.0).toFloat()
                val easedProgress = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1f).transform(progress)

                // More pronounced arc/wobble
                val sideVelocity = if (point.id % 2 == 0L) 1f else -1f
                val wobbleX = sideVelocity * kotlin.math.sin(progress * kotlin.math.PI).toFloat() * 60.dp.toPx()
                val wobbleY = -kotlin.math.sin(progress * kotlin.math.PI).toFloat() * 30.dp.toPx() // Slight upward arc

                val currentX = point.startPos.x + (point.targetPos.x - point.startPos.x) * easedProgress + wobbleX
                val currentY = point.startPos.y + (point.targetPos.y - point.startPos.y) * easedProgress + wobbleY

                val alpha = if (progress > 0.8f) (1f - progress) * 5f else 1f
                val scale =
                    if (progress < 0.2f) {
                        progress * 5f
                    } else if (progress > 0.8f) {
                        (1f - progress) * 5f
                    } else {
                        1f
                    }

                // Draw a simple glow/trail
                drawCircle(
                    color = point.color.copy(alpha = (alpha * 0.3f).coerceIn(0f, 1f)),
                    center = Offset(currentX, currentY),
                    radius = point.size * scale * 1.5f,
                )

                // The particle itself (looks like a gold coin/spark)
                drawCircle(
                    color = point.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    center = Offset(currentX, currentY),
                    radius = point.size * scale / 2,
                )
            }
        }
    }
}

// Helper to avoid issues with basic list addition in state
private fun <T> listListOf(vararg elements: T): List<T> = listOf(*elements)
