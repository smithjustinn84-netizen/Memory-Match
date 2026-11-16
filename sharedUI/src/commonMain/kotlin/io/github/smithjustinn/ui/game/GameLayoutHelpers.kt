package io.github.smithjustinn.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.HeatAmber
import io.github.smithjustinn.theme.HeatCoral
import io.github.smithjustinn.theme.HeatGold
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.game.components.effects.CardFountainOverlay
import io.github.smithjustinn.ui.game.components.effects.ConfettiEffect
import io.github.smithjustinn.ui.game.components.hud.NewHighScoreSnackbar
import io.github.smithjustinn.ui.game.components.hud.ResultsCard
import io.github.smithjustinn.utils.Constants
import kotlinx.coroutines.launch

private const val HEAT_TRANSITION_DURATION_MS = 800
private const val FELT_RADIUS_WIDTH_FRACTION = 1.2f
private const val FELT_RADIUS_HEIGHT_FRACTION = 0.7f
private const val FELT_FOCUS_X = 0.5f
private const val FELT_FOCUS_Y = 0.45f
private const val FELT_MID_STOP = 0.5f
private const val RESULTS_SCRIM_ALPHA = 0.86f
private const val SNACKBAR_TOP_PADDING = 16
private const val SNACKBAR_HORIZONTAL_PADDING = 16
private const val SNACKBAR_MAX_WIDTH = 500
private const val RESULTS_MAX_WIDTH = 550

/**
 * The table: one felt gradient, lit slightly above centre and falling off to the rail colour.
 * In heat mode the felt tokens are the ember palette, so the same gradient cross-fades to red.
 */
@Composable
fun GameBackground(
    isHeatMode: Boolean,
    comboMultiplier: Int,
) {
    val colors = PokerTheme.colors
    val transition = tween<Color>(durationMillis = HEAT_TRANSITION_DURATION_MS)

    val centerColor by animateColorAsState(colors.feltGreenCenter, transition, label = "feltCenter")
    val feltColor by animateColorAsState(colors.feltGreen, transition, label = "felt")
    val edgeColor by animateColorAsState(colors.feltGreenDark, transition, label = "feltEdge")

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .drawBehind { drawFelt(centerColor, feltColor, edgeColor) },
    ) {
        if (isHeatMode) {
            AuroraEffect(
                baseColor = HeatAmber,
                accentColor = HeatCoral,
                highlightColor = HeatGold,
                height = (250 + comboMultiplier * 10).coerceAtMost(500).dp,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** An elliptical radial gradient: a circular one, stretched horizontally around its focus. */
private fun DrawScope.drawFelt(
    centerColor: Color,
    feltColor: Color,
    edgeColor: Color,
) {
    drawRect(edgeColor)

    val radius = size.height * FELT_RADIUS_HEIGHT_FRACTION
    if (radius > 0f) {
        val focus = Offset(size.width * FELT_FOCUS_X, size.height * FELT_FOCUS_Y)
        val stretch = size.width * FELT_RADIUS_WIDTH_FRACTION / radius
        scale(scaleX = stretch, scaleY = 1f, pivot = focus) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        0f to centerColor,
                        FELT_MID_STOP to feltColor,
                        1f to edgeColor,
                        center = focus,
                        radius = radius,
                    ),
                radius = radius,
                center = focus,
            )
        }
    }
}

@Composable
fun GameGameOverOverlay(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PokerTheme.colors.oakWood.copy(alpha = RESULTS_SCRIM_ALPHA)),
    ) {
        // Main content
        GameResultsOverlay(
            state = state,
            component = component,
            useCompactUI = useCompactUI,
        )

        if (state.game.isGameWon) {
            // Celebration effects - now above the results card
            CardFountainOverlay(
                cards = state.game.cards,
                theme = state.cardTheme,
                areSuitsMultiColored = state.areSuitsMultiColored,
            )

            ConfettiEffect()

            if (state.isNewHighScore) {
                NewHighScoreSnackbar(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(
                                top = SNACKBAR_TOP_PADDING.dp,
                                start = SNACKBAR_HORIZONTAL_PADDING.dp,
                                end = SNACKBAR_HORIZONTAL_PADDING.dp,
                            ).widthIn(max = SNACKBAR_MAX_WIDTH.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.GameResultsOverlay(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    val hapticsService = graph.hapticsService
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    ResultsCard(
        isWon = state.game.isGameWon,
        isBusted = state.game.isBusted,
        highScore = maxOf(state.game.score, state.bestScore),
        moves = state.game.moves,
        elapsedTimeSeconds = state.elapsedTimeSeconds,
        scoreBreakdown = state.game.scoreBreakdown,
        onPlayAgain = {
            hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
            audioService.playEffect(AudioService.SoundEffect.CLICK)
            component.onRestart()
            audioService.startMusic()
        },
        onShareReplay = {
            audioService.playEffect(AudioService.SoundEffect.CLICK)
            val seed = state.game.seed ?: 0L
            val link =
                "${Constants.DEEP_LINK_PREFIX}?" +
                    "${Constants.QUERY_PARAM_MODE}=${state.game.mode}&" +
                    "${Constants.QUERY_PARAM_PAIRS}=${state.game.pairCount}&" +
                    "${Constants.QUERY_PARAM_SEED}=$seed"
            scope.launch {
                clipboardManager.setText(AnnotatedString(link))
            }
            hapticsService.vibrateMatch()
        },
        onScoreTick = { hapticsService.vibrateTick() },
        onBackToLobby = {
            audioService.playEffect(AudioService.SoundEffect.CLICK)
            component.onBack()
        },
        onDoubleRewards = { component.onDoubleRewardsAd() },
        canDoubleRewards = state.canDoubleRewards,
        modifier =
            Modifier
                .align(Alignment.Center)
                .widthIn(max = RESULTS_MAX_WIDTH.dp)
                .padding(
                    vertical = if (useCompactUI) PokerTheme.spacing.small else PokerTheme.spacing.large,
                ),
        mode = state.game.mode,
    )
}

@Composable
fun HeatModeTransitionHandler(
    isHeatMode: Boolean,
    onHeatLost: () -> Unit,
) {
    val currentOnHeatLost by rememberUpdatedState(onHeatLost)
    var lastHeatMode by remember { mutableStateOf(isHeatMode) }
    LaunchedEffect(isHeatMode) {
        if (lastHeatMode && !isHeatMode) {
            currentOnHeatLost()
        }
        lastHeatMode = isHeatMode
    }
}

private const val SHAKE_MAX_OFFSET = 20f
private const val SHAKE_MID_OFFSET = 10f
private const val SHAKE_RESET_OFFSET = 0f

suspend fun runShakeAnimation(animatable: Animatable<Float, *>) {
    animatable.animateTo(SHAKE_MAX_OFFSET, spring(stiffness = Spring.StiffnessHigh))
    animatable.animateTo(-SHAKE_MAX_OFFSET, spring(stiffness = Spring.StiffnessHigh))
    animatable.animateTo(SHAKE_MID_OFFSET, spring(stiffness = Spring.StiffnessHigh))
    animatable.animateTo(SHAKE_RESET_OFFSET, spring(stiffness = Spring.StiffnessMedium))
}
