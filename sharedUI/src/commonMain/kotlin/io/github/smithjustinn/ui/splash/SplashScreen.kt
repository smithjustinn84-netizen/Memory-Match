package io.github.smithjustinn.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.splash_skip
import io.github.smithjustinn.resources.splash_title
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.theme.SplashCurtainGreen
import io.github.smithjustinn.ui.assets.getPreferredColor
import io.github.smithjustinn.ui.assets.toColor
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.components.GlimmerText
import io.github.smithjustinn.ui.components.pokerBackground
import io.github.smithjustinn.ui.game.components.cards.CardBack
import io.github.smithjustinn.ui.game.components.cards.CardFace
import io.github.smithjustinn.ui.game.components.cards.FULL_ROTATION
import io.github.smithjustinn.ui.game.components.cards.HALF_ROTATION
import io.github.smithjustinn.ui.game.components.cards.calculateSuitColor
import io.github.smithjustinn.ui.game.components.grid.CARD_ASPECT_RATIO
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.sin

// --- Timeline (ms from the first frame) ---
// One clock drives every beat: lights up -> deal -> flip -> match -> wordmark.
private const val CURTAIN_MS = 400
private const val DEAL_LEFT_AT_MS = 150
private const val DEAL_RIGHT_AT_MS = 290
private const val DEAL_MS = 420
private const val DEAL_FADE_MS = 120
private const val FLIP_LEFT_AT_MS = 650
private const val FLIP_RIGHT_AT_MS = 820
private const val FLIP_MS = 380
private const val MATCH_AT_MS = 1250
private const val MATCH_MS = 450
private const val BLOOM_RISE_MS = 260
private const val BLOOM_SETTLE_MS = 600
private const val TITLE_AT_MS = 1300
private const val TITLE_MS = 450
private const val ORNAMENT_AT_MS = 1450
private const val ORNAMENT_MS = 500
private const val TOTAL_MS = 2200

// --- Card sizing (scales with the window so landscape phones and desktop both work) ---
private const val CARD_WIDTH_OF_HEIGHT = 0.2f
private const val CARD_WIDTH_OF_WIDTH = 0.36f
private const val CARD_MIN_WIDTH_DP = 80
private const val CARD_MAX_WIDTH_DP = 140
private const val PAIR_WIDTH_FACTOR = 2.2f
private const val CARD_ELEVATION_DP = 10
private const val CAMERA_DISTANCE = 12f

// --- Card motion (offsets are fractions of the card's own size) ---
private const val DEAL_TRAVEL_FACTOR = 2.5f
private const val DEAL_START_SCALE = 1.2f
private const val DEAL_SPIN_DEG = 24f
private const val DEALT_TILT_DEG = 2f
private const val DEALT_OFFSET_FACTOR = 0.56f
private const val FANNED_TILT_DEG = 12f
private const val FANNED_OFFSET_FACTOR = 0.27f
private const val FLIP_POP_SCALE = 0.08f
private const val MATCH_POP_SCALE = 0.06f

// --- Match bloom ---
private const val BLOOM_RADIUS_FACTOR = 1.05f
private const val BLOOM_START_SCALE = 0.5f
private const val BLOOM_CORE_ALPHA = 0.55f
private const val BLOOM_MID_ALPHA = 0.18f
private const val BLOOM_REST_ALPHA = 0.45f

// --- Wordmark ---
private const val TITLE_RISE_DP = 14
private const val ORNAMENT_WIDTH_DP = 148
private const val ORNAMENT_HEIGHT_DP = 8
private const val ORNAMENT_GAP_DP = 8

@Immutable
private data class SplashCardSpec(
    val suit: Suit,
    val side: Float,
    val dealAtMs: Int,
    val flipAtMs: Int,
)

private val LeftCard = SplashCardSpec(Suit.Hearts, side = -1f, dealAtMs = DEAL_LEFT_AT_MS, flipAtMs = FLIP_LEFT_AT_MS)
private val RightCard = SplashCardSpec(Suit.Spades, side = 1f, dealAtMs = DEAL_RIGHT_AT_MS, flipAtMs = FLIP_RIGHT_AT_MS)

/**
 * Splash: the game's core loop played once in miniature.
 *
 * Two cards are dealt face-down, flipped, and matched; the wordmark resolves under the pair, which
 * settles into the same fanned pose the Start screen header uses so the hand-off reads as one move.
 * Tapping anywhere skips straight to the app.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    val clock = remember { Animatable(0f) }
    LaunchedEffect(clock) {
        clock.animateTo(TOTAL_MS.toFloat(), tween(TOTAL_MS, easing = LinearEasing))
        currentOnFinished()
    }

    // Read only inside draw/layer lambdas, so the sequence never recomposes per frame.
    val time = remember(clock) { { clock.value } }
    val skipLabel = stringResource(Res.string.splash_skip)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().pokerBackground(),
        contentAlignment = Alignment.Center,
    ) {
        val cardWidth =
            minOf(maxHeight * CARD_WIDTH_OF_HEIGHT, maxWidth * CARD_WIDTH_OF_WIDTH)
                .coerceIn(CARD_MIN_WIDTH_DP.dp, CARD_MAX_WIDTH_DP.dp)

        AuroraEffect(modifier = Modifier.align(Alignment.BottomCenter))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MatchedPair(time = time, cardWidth = cardWidth)
            Spacer(modifier = Modifier.height(PokerTheme.spacing.extraLarge))
            SplashWordmark(time = time)
        }

        // Curtain: starts as the Android system-splash colour and lifts, so launch has no colour jump.
        // It sits on top, so it also owns tap-to-skip.
        Spacer(
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val curtain = 1f - segment(time(), 0, CURTAIN_MS, LinearEasing)
                        if (curtain > 0f) drawRect(SplashCurtainGreen, alpha = curtain)
                    }.clickable(
                        interactionSource = null,
                        indication = null,
                        onClickLabel = skipLabel,
                        onClick = { currentOnFinished() },
                    ),
        )
    }
}

@Composable
private fun MatchedPair(
    time: () -> Float,
    cardWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(cardWidth * PAIR_WIDTH_FACTOR).clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        MatchBloom(time = time, modifier = Modifier.matchParentSize())
        SplashCard(spec = RightCard, time = time, cardWidth = cardWidth)
        SplashCard(spec = LeftCard, time = time, cardWidth = cardWidth)
    }
}

/** Warm gold light that blooms behind the pair at the moment of the match, then rests. */
@Composable
private fun MatchBloom(
    time: () -> Float,
    modifier: Modifier = Modifier,
) {
    val gold = PokerTheme.colors.goldenYellow
    Spacer(
        modifier =
            modifier.drawWithCache {
                val radius = size.maxDimension * BLOOM_RADIUS_FACTOR
                val brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                gold.copy(alpha = BLOOM_CORE_ALPHA),
                                gold.copy(alpha = BLOOM_MID_ALPHA),
                                Color.Transparent,
                            ),
                        radius = radius,
                    )
                onDrawBehind {
                    val now = time()
                    val rise = segment(now, MATCH_AT_MS, BLOOM_RISE_MS, LinearOutSlowInEasing)
                    val settle = segment(now, MATCH_AT_MS + BLOOM_RISE_MS, BLOOM_SETTLE_MS)
                    val alpha = rise * lerp(1f, BLOOM_REST_ALPHA, settle)
                    if (alpha > 0f) {
                        scale(lerp(BLOOM_START_SCALE, 1f, rise)) {
                            drawCircle(brush = brush, radius = radius, alpha = alpha)
                        }
                    }
                }
            },
    )
}

/**
 * A display-only card (no click, hover, or haptics) wearing the player's equipped back and skin.
 */
@Composable
private fun SplashCard(
    spec: SplashCardSpec,
    time: () -> Float,
    cardWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val cardTheme = PokerTheme.cardTheme
    val backColor = cardTheme.backColorHex?.toColor() ?: cardTheme.back.getPreferredColor()
    val suitColor = calculateSuitColor(spec.suit, areSuitsMultiColored = false, theme = cardTheme.skin)
    val shape = PokerTheme.shapes.medium

    val flipRotation =
        remember(spec, time) {
            derivedStateOf { FULL_ROTATION * (1f - segment(time(), spec.flipAtMs, FLIP_MS)) }
        }
    val showFace by remember(flipRotation) { derivedStateOf { flipRotation.value <= HALF_ROTATION } }

    Box(
        modifier =
            modifier
                .width(cardWidth)
                .aspectRatio(CARD_ASPECT_RATIO)
                .graphicsLayer {
                    applyCardMotion(spec = spec, timeMs = time(), flipRotation = flipRotation.value)
                    shadowElevation = CARD_ELEVATION_DP.dp.toPx()
                    this.shape = shape
                    clip = true
                }.background(if (showFace) Color.White else backColor),
    ) {
        if (showFace) {
            CardFace(rank = Rank.Ace, suit = spec.suit, suitColor = suitColor, theme = cardTheme.skin)
        } else {
            CardBack(theme = cardTheme.back, backColor = backColor, rotation = flipRotation)
        }
    }
}

private fun GraphicsLayerScope.applyCardMotion(
    spec: SplashCardSpec,
    timeMs: Float,
    flipRotation: Float,
) {
    val deal = segment(timeMs, spec.dealAtMs, DEAL_MS, LinearOutSlowInEasing)
    val gather = segment(timeMs, MATCH_AT_MS, MATCH_MS)
    val flipPop = pulse(segment(timeMs, spec.flipAtMs, FLIP_MS, LinearEasing))
    val matchPop = pulse(segment(timeMs, MATCH_AT_MS, MATCH_MS, LinearEasing))

    // Dealt side by side, then drawn together into an overlapping fan when they match.
    translationX = spec.side * size.width * lerp(DEALT_OFFSET_FACTOR, FANNED_OFFSET_FACTOR, gather)
    translationY = (1f - deal) * size.height * DEAL_TRAVEL_FACTOR
    rotationZ = spec.side * lerp(lerp(DEAL_SPIN_DEG, DEALT_TILT_DEG, deal), FANNED_TILT_DEG, gather)
    rotationY = flipRotation
    cameraDistance = CAMERA_DISTANCE * density

    val cardScale = lerp(DEAL_START_SCALE, 1f, deal) + FLIP_POP_SCALE * flipPop + MATCH_POP_SCALE * matchPop
    scaleX = cardScale
    scaleY = cardScale
    alpha = segment(timeMs, spec.dealAtMs, DEAL_FADE_MS, LinearEasing)
}

@Composable
private fun SplashWordmark(
    time: () -> Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.graphicsLayer {
                val reveal = segment(time(), TITLE_AT_MS, TITLE_MS, LinearOutSlowInEasing)
                alpha = reveal
                translationY = (1f - reveal) * TITLE_RISE_DP.dp.toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlimmerText(
            text = stringResource(Res.string.splash_title),
            modifier = Modifier.semantics { heading() },
            fontSize = 28.sp,
            letterSpacing = 3.sp,
        )
        Spacer(modifier = Modifier.height(PokerTheme.spacing.medium))
        SplashOrnament(time = time)
    }
}

/** A gold diamond with hairlines that draw outward from it. */
@Composable
private fun SplashOrnament(
    time: () -> Float,
    modifier: Modifier = Modifier,
) {
    val gold = PokerTheme.colors.goldenYellow
    Spacer(
        modifier =
            modifier
                .size(width = ORNAMENT_WIDTH_DP.dp, height = ORNAMENT_HEIGHT_DP.dp)
                .drawWithCache {
                    val gem = size.height / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val lineStart = gem + ORNAMENT_GAP_DP.dp.toPx()
                    val lineLength = size.width / 2 - lineStart
                    val stroke = 1.dp.toPx()
                    val diamond =
                        Path().apply {
                            moveTo(center.x, center.y - gem)
                            lineTo(center.x + gem, center.y)
                            lineTo(center.x, center.y + gem)
                            lineTo(center.x - gem, center.y)
                            close()
                        }
                    // Fades to nothing at the outer ends.
                    val lineBrush = Brush.horizontalGradient(listOf(Color.Transparent, gold, gold, Color.Transparent))

                    onDrawBehind {
                        val grow = segment(time(), ORNAMENT_AT_MS, ORNAMENT_MS, LinearOutSlowInEasing)
                        if (grow > 0f) {
                            val reach = lineLength * grow
                            drawPath(diamond, gold, alpha = grow)
                            for (side in listOf(-1f, 1f)) {
                                drawLine(
                                    brush = lineBrush,
                                    start = Offset(center.x + side * lineStart, center.y),
                                    end = Offset(center.x + side * (lineStart + reach), center.y),
                                    strokeWidth = stroke,
                                )
                            }
                        }
                    }
                },
    )
}

/** Eased 0..1 progress of the window that starts at [startMs] and lasts [durationMs]. */
private fun segment(
    timeMs: Float,
    startMs: Int,
    durationMs: Int,
    easing: Easing = FastOutSlowInEasing,
): Float = easing.transform(((timeMs - startMs) / durationMs).coerceIn(0f, 1f))

/** Maps linear 0..1 progress to a 0 -> 1 -> 0 bump. */
private fun pulse(progress: Float): Float = sin(PI.toFloat() * progress)
