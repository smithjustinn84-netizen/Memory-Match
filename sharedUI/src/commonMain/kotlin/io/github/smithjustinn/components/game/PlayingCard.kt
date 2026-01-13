package io.github.smithjustinn.components.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import kotlin.math.roundToInt

@Composable
fun PlayingCard(
    suit: Suit,
    rank: Rank,
    isFaceUp: Boolean,
    isMatched: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    backColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 0f else 180f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isMatched -> 1.05f
            isHovered && !isFaceUp -> 1.03f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pulse"
    )

    val hoverBorderColor by animateColorAsState(
        targetValue = if (isHovered && !isFaceUp) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        label = "hoverBorderColor"
    )

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            repeat(3) {
                shakeOffset.animateTo(10f, tween(50))
                shakeOffset.animateTo(-10f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    // Playing cards traditionally have a white face. We force this for the "classic" look.
    // We use fixed dark colors for the suits to ensure they remain visible on the white background.
    val cardFaceColor = Color.White
    val suitColor = if (suit.isRed) Color(0xFFBB152C) else Color(0xFF1A1C1C)
    val errorColor = Color(0xFFBB152C)

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .width(80.dp)
            .height(120.dp)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .graphicsLayer {
                rotationY = rotation
                scaleX = scale
                scaleY = scale
                cameraDistance = 12f * density
            },
        colors = CardDefaults.cardColors(
            containerColor = if (rotation <= 90f) cardFaceColor else backColor
        ),
        border = if (rotation <= 90f) {
            when {
                isMatched -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary) // Theme-aware success
                isError -> BorderStroke(2.dp, errorColor) // Fixed high-contrast error
                else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            }
        } else {
            BorderStroke(if (isHovered) 5.dp else 4.dp, hoverBorderColor)
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (rotation <= 90f) {
                CardFace(rank = rank, suit = suit, suitColor = suitColor)
                
                if (isMatched) {
                    ShimmerEffect()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                ) {
                    GeometricCardBack()
                }
            }
        }
    }
}

@Composable
private fun CardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color
) {
    Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        Text(
            text = rank.symbol,
            color = suitColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Text(
            text = suit.symbol,
            color = suitColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Text(
            text = suit.symbol,
            color = suitColor,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.align(Alignment.Center)
        )

        Text(
            text = rank.symbol,
            color = suitColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun ShimmerEffect() {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.6f),
        Color.White.copy(alpha = 0.0f),
    )

    val translateAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        translateAnim.animateTo(
            targetValue = 1000f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
    }

    if (translateAnim.value < 1000f) {
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        )
    }
}

@Composable
private fun GeometricCardBack() {
    val patternColor = Color.White.copy(alpha = 0.3f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 20.dp.toPx()
        val path = Path()
        
        for (x in 0 until size.width.toInt() step step.toInt()) {
            for (y in 0 until size.height.toInt() step step.toInt()) {
                path.moveTo(x.toFloat(), y.toFloat() + step / 2)
                path.lineTo(x.toFloat() + step / 2, y.toFloat())
                path.lineTo(x.toFloat() + step, y.toFloat() + step / 2)
                path.lineTo(x.toFloat() + step / 2, y.toFloat() + step)
                path.close()
            }
        }
        drawPath(path, color = patternColor, style = Stroke(width = 2.dp.toPx()))
    }
}
