package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.ChipBlue
import io.github.smithjustinn.theme.ChipGreen
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.ShopIcons
import kotlin.math.abs

internal const val CARD_CORNER_RADIUS_DP = 10
internal const val CARD_BORDER_WIDTH_DP = 3

private const val SHADOW_AMBIENT_ALPHA = 0.3f
private const val SHADOW_SPOT_ALPHA = 0.6f
private const val RIM_LIGHT_THRESHOLD = 0.1f
private const val BORDER_SIZE_MULTIPLIER = 2f
private const val FACE_EDGE_ALPHA = 0.12f

private const val SLOT_STROKE_DP = 1.5f
private const val SLOT_DASH_DP = 5
private const val SLOT_GAP_DP = 4
private const val SLOT_OUTLINE_ALPHA = 0.24f
private const val SLOT_FILL_ALPHA = 0.35f

private const val BADGE_SIZE_DP = 20
private const val BADGE_OVERHANG_DP = 6

@Composable
@Suppress("ktlint:compose:state-param-check")
fun CardShadowLayer(
    elevation: State<Dp>,
    yOffset: State<Dp>,
    isRecentlyMatched: Boolean,
) {
    val glowColor = PokerTheme.colors.goldenYellow
    val baseShadowColor = if (isRecentlyMatched) glowColor else PokerTheme.colors.tableShadow
    val shadowShape = remember { RoundedCornerShape(CARD_CORNER_RADIUS_DP.dp) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = yOffset.value.toPx()
                    shadowElevation = elevation.value.toPx()
                    shape = shadowShape
                    clip = false
                    ambientShadowColor = baseShadowColor.copy(alpha = SHADOW_AMBIENT_ALPHA)
                    spotShadowColor = baseShadowColor.copy(alpha = SHADOW_SPOT_ALPHA)
                },
    )
}

/** Draws a stroke that sits fully inside the card's rounded outline. */
private fun DrawScope.drawInsetCardStroke(
    color: Color,
    width: Float,
) {
    val radius = (CARD_CORNER_RADIUS_DP.dp.toPx() - width / 2).coerceAtLeast(0f)
    drawRoundRect(
        color = color,
        topLeft = Offset(width / 2, width / 2),
        size = Size(size.width - width, size.height - width),
        cornerRadius = CornerRadius(radius),
        style = Stroke(width = width),
    )
}

@Composable
@Suppress("ktlint:compose:state-param-check")
fun Modifier.cardBorder(
    rotation: State<Float>,
    visualState: CardVisualState,
): Modifier {
    val primaryColor = PokerTheme.colors.goldenYellow
    val errorColor = MaterialTheme.colorScheme.error
    val faceEdgeColor = PokerTheme.colors.ink.copy(alpha = FACE_EDGE_ALPHA)

    return this.drawWithContent {
        drawContent()

        val currentRotation = rotation.value

        if (currentRotation <= HALF_ROTATION) {
            val (width, color) =
                when {
                    visualState.isRecentlyMatched -> CARD_BORDER_WIDTH_DP.dp to primaryColor
                    visualState.isMatched -> 1.dp to primaryColor.copy(alpha = MEDIUM_ALPHA)
                    visualState.isError -> CARD_BORDER_WIDTH_DP.dp to errorColor
                    else -> 1.dp to faceEdgeColor
                }

            drawInsetCardStroke(color = color, width = width.toPx())
        } else {
            // The back paints its own card-stock border; this only adds the rim light mid-flip.
            val rimLightAlpha = (1f - abs(currentRotation - HALF_ROTATION) / HALF_ROTATION).coerceIn(0f, 1f)

            if (rimLightAlpha > RIM_LIGHT_THRESHOLD) {
                val width = CARD_BORDER_WIDTH_DP.dp + (rimLightAlpha * BORDER_SIZE_MULTIPLIER).dp
                drawInsetCardStroke(
                    color = Color.White.copy(alpha = rimLightAlpha * HIGH_ALPHA),
                    width = width.toPx(),
                )
            }
        }
    }
}

/** The dashed outline left on the felt where a matched pair has been cleared. */
@Composable
internal fun EmptyCardSlot(modifier: Modifier = Modifier) {
    val outline = PokerTheme.colors.ivory.copy(alpha = SLOT_OUTLINE_ALPHA)
    val fill = PokerTheme.colors.feltGreenDark.copy(alpha = SLOT_FILL_ALPHA)

    Spacer(
        modifier =
            modifier
                .fillMaxSize()
                .drawBehind {
                    val stroke = SLOT_STROKE_DP.dp.toPx()
                    val radius = CornerRadius(CARD_CORNER_RADIUS_DP.dp.toPx())
                    drawRoundRect(color = fill, cornerRadius = radius)
                    drawRoundRect(
                        color = outline,
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = radius,
                        style =
                            Stroke(
                                width = stroke,
                                pathEffect =
                                    PathEffect.dashPathEffect(
                                        floatArrayOf(SLOT_DASH_DP.dp.toPx(), SLOT_GAP_DP.dp.toPx()),
                                    ),
                            ),
                    )
                },
    )
}

/** Gold check that marks a fresh match without relying on colour alone. Decorative only. */
@Composable
internal fun MatchedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .offset(x = BADGE_OVERHANG_DP.dp, y = (-BADGE_OVERHANG_DP).dp)
                .size(BADGE_SIZE_DP.dp)
                .background(PokerTheme.colors.onGold, CircleShape),
    ) {
        Icon(
            imageVector = ShopIcons.CheckCircle,
            contentDescription = null,
            tint = PokerTheme.colors.goldenYellow,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun calculateSuitColor(
    suit: Suit,
    areSuitsMultiColored: Boolean,
    theme: CardSymbolTheme,
): Color {
    val ink = PokerTheme.colors.ink
    val red = PokerTheme.colors.tacticalRed

    return if (areSuitsMultiColored && theme != CardSymbolTheme.POKER) {
        when (suit) {
            Suit.Hearts -> red
            Suit.Diamonds -> ChipBlue
            Suit.Clubs -> ChipGreen
            Suit.Spades -> ink
        }
    } else {
        if (suit.isRed) red else ink
    }
}
