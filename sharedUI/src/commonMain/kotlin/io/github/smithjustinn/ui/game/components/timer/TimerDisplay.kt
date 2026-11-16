package io.github.smithjustinn.ui.game.components.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.stats_time_header
import io.github.smithjustinn.resources.timer_minus_seconds_format
import io.github.smithjustinn.resources.timer_plus_seconds_format
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.game.components.hud.HudStatLabel
import io.github.smithjustinn.ui.game.components.hud.hudNumeralStyle
import io.github.smithjustinn.utils.formatTime
import org.jetbrains.compose.resources.stringResource

private const val COLOR_TRANSITION_DURATION_MS = 500
private const val PULSE_SCALE_TARGET = 1.15f
private const val PIVOT_CENTER = 0.5f

@Composable
fun TimerDisplay(
    infiniteTransition: InfiniteTransition,
    state: TimerState,
    feedback: TimerFeedback,
    modifier: Modifier = Modifier,
    layout: TimerLayout = TimerLayout.STANDARD,
) {
    val timerColorState =
        animateTimerColor(
            showTimeLoss = feedback.showTimeLoss,
            showTimeGain = feedback.showTimeGain,
            isMegaBonus = feedback.isMegaBonus,
            isLowTime = state.isLowTime,
        )

    val timerScale =
        calculateTimerScale(
            infiniteTransition = infiniteTransition,
            showTimeLoss = feedback.showTimeLoss,
            isCriticalTime = state.isCriticalTime,
        )

    val visuals =
        TimerVisuals(
            color = timerColorState,
            scale = timerScale,
            layout = layout,
        )

    if (layout == TimerLayout.MINIMAL) {
        MinimalTimerDisplay(
            state = state,
            feedback = feedback,
            visuals = visuals,
            modifier = modifier,
        )
    } else {
        StandardTimerDisplay(
            state = state,
            feedback = feedback,
            visuals = visuals,
            modifier = modifier,
        )
    }
}

@Composable
private fun animateTimerColor(
    showTimeLoss: Boolean,
    showTimeGain: Boolean,
    isMegaBonus: Boolean,
    isLowTime: Boolean,
): State<Color> {
    val targetValue =
        when {
            showTimeLoss -> PokerTheme.colors.tacticalRed
            showTimeGain && isMegaBonus -> PokerTheme.colors.goldenYellow
            showTimeGain -> PokerTheme.colors.bonusGreen
            isLowTime -> PokerTheme.colors.tacticalRed
            else -> PokerTheme.colors.ivory
        }

    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = if (showTimeGain || showTimeLoss) 100 else COLOR_TRANSITION_DURATION_MS),
    )
}

@Composable
private fun calculateTimerScale(
    infiniteTransition: InfiniteTransition,
    showTimeLoss: Boolean,
    isCriticalTime: Boolean,
): State<Float> {
    val infinitePulseScaleState =
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = PULSE_SCALE_TARGET,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(COLOR_TRANSITION_DURATION_MS, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
        )

    val lossPulseScaleState =
        animateFloatAsState(
            targetValue = if (showTimeLoss) 1.2f else 1f,
            animationSpec =
                if (showTimeLoss) {
                    spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
                } else {
                    spring(stiffness = Spring.StiffnessLow)
                },
        )

    return remember(showTimeLoss, isCriticalTime) {
        derivedStateOf {
            when {
                showTimeLoss -> lossPulseScaleState.value
                isCriticalTime -> infinitePulseScaleState.value
                else -> 1f
            }
        }
    }
}

@Composable
private fun MinimalTimerDisplay(
    state: TimerState,
    feedback: TimerFeedback,
    visuals: TimerVisuals,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        TimerDigits(
            time = state.time,
            visuals = visuals,
            style = hudNumeralStyle().copy(fontSize = 14.sp, lineHeight = 18.sp),
        )

        AnimatedVisibility(
            visible = feedback.showTimeGain,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
        ) {
            Text(
                text = stringResource(Res.string.timer_plus_seconds_format, feedback.timeGainAmount),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color =
                    if (feedback.isMegaBonus) {
                        PokerTheme.colors.goldenYellow
                    } else {
                        PokerTheme.colors.bonusGreen
                    },
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun StandardTimerDisplay(
    state: TimerState,
    feedback: TimerFeedback,
    visuals: TimerVisuals,
    modifier: Modifier = Modifier,
) {
    // A HUD stat: small caption (with the gain/loss feedback beside it) over a mono numeral.
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HudStatLabel(text = stringResource(Res.string.stats_time_header))

            TimeGainIndicator(
                showTimeGain = feedback.showTimeGain,
                timeGainAmount = feedback.timeGainAmount,
                isMegaBonus = feedback.isMegaBonus,
            )

            TimeLossIndicator(
                showTimeLoss = feedback.showTimeLoss,
                timeLossAmount = feedback.timeLossAmount,
            )
        }

        TimerDigits(
            time = state.time,
            visuals = visuals,
            style = hudNumeralStyle(dense = visuals.layout == TimerLayout.COMPACT),
        )
    }
}

@Composable
private fun TimerDigits(
    time: Long,
    visuals: TimerVisuals,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = formatTime(time),
        style = style,
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = visuals.scale.value
                    scaleY = visuals.scale.value
                    transformOrigin = TransformOrigin(0f, PIVOT_CENTER)
                },
        color = visuals.color.value,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun TimeGainIndicator(
    showTimeGain: Boolean,
    timeGainAmount: Int,
    isMegaBonus: Boolean,
) {
    AnimatedVisibility(
        visible = showTimeGain,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
    ) {
        Text(
            text = stringResource(Res.string.timer_plus_seconds_format, timeGainAmount),
            style = feedbackStyle(),
            color = if (isMegaBonus) PokerTheme.colors.goldenYellow else PokerTheme.colors.bonusGreen,
            maxLines = 1,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun TimeLossIndicator(
    showTimeLoss: Boolean,
    timeLossAmount: Long,
) {
    AnimatedVisibility(
        visible = showTimeLoss,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
        Text(
            text = stringResource(Res.string.timer_minus_seconds_format, timeLossAmount),
            style = feedbackStyle(),
            color = PokerTheme.colors.tacticalRed,
            maxLines = 1,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun feedbackStyle(): TextStyle =
    PokerTheme.typography.labelSmall.copy(
        fontFamily = PokerTheme.fonts.mono,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
    )
