package io.github.smithjustinn.components.game

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardState
import kotlin.math.roundToInt
import kotlin.random.Random

private val CARD_WIDTH = 80.dp
private val CARD_HEIGHT = 120.dp

/**
 * Internal state for a bouncing card animation.
 */
@Stable
private class BouncingCard(
    val card: CardState,
    initialX: Float,
    initialY: Float,
    initialVx: Float,
    initialVy: Float,
    val vRot: Float = (Random.nextFloat() - 0.5f) * 5f
) {
    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)
    var vx = initialVx
    var vy = initialVy
    var rotation by mutableStateOf(0f)

    fun update(widthPx: Float, heightPx: Float, cardWidthPx: Float, cardHeightPx: Float) {
        x += vx
        y += vy
        rotation += vRot

        val maxX = (widthPx - cardWidthPx).coerceAtLeast(0f)
        val maxY = (heightPx - cardHeightPx).coerceAtLeast(0f)

        if (x <= 0f || x >= maxX) {
            vx = -vx
            x = x.coerceIn(0f, maxX)
        }
        if (y <= 0f || y >= maxY) {
            vy = -vy
            y = y.coerceIn(0f, maxY)
        }
    }
}

@Composable
fun BouncingCardsOverlay(
    cards: List<CardState>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val cardWidthPx = with(density) { CARD_WIDTH.toPx() }
        val cardHeightPx = with(density) { CARD_HEIGHT.toPx() }

        val bouncingCards = remember { mutableStateListOf<BouncingCard>() }

        // Initialize bouncing cards when the input list changes
        LaunchedEffect(cards) {
            if (bouncingCards.isEmpty() && cards.isNotEmpty()) {
                cards.forEach { card ->
                    bouncingCards.add(
                        BouncingCard(
                            card = card,
                            initialX = Random.nextFloat() * (widthPx - cardWidthPx).coerceAtLeast(0f),
                            initialY = Random.nextFloat() * (heightPx - cardHeightPx).coerceAtLeast(0f),
                            initialVx = (Random.nextFloat() - 0.5f) * 15f,
                            initialVy = (Random.nextFloat() - 0.5f) * 15f
                        )
                    )
                }
            }
        }

        // Animation loop using withFrameNanos for smooth movement
        LaunchedEffect(widthPx, heightPx) {
            while (true) {
                withFrameNanos {
                    bouncingCards.forEach { it.update(widthPx, heightPx, cardWidthPx, cardHeightPx) }
                }
            }
        }

        bouncingCards.forEach { bCard ->
            key(bCard.card.id) {
                PlayingCard(
                    suit = bCard.card.suit,
                    rank = bCard.card.rank,
                    isFaceUp = true,
                    isMatched = true,
                    modifier = Modifier
                        .offset { IntOffset(bCard.x.roundToInt(), bCard.y.roundToInt()) }
                        .graphicsLayer { rotationZ = bCard.rotation }
                )
            }
        }
    }
}
