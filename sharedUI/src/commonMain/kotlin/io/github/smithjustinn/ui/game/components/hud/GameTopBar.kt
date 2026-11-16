package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.combo_badge_format
import io.github.smithjustinn.resources.hud_heat_label
import io.github.smithjustinn.resources.hud_pot_label
import io.github.smithjustinn.resources.hud_score_label
import io.github.smithjustinn.resources.mute_content_description
import io.github.smithjustinn.resources.restart_content_description
import io.github.smithjustinn.resources.unmute_content_description
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.extensions.pokerClickable
import io.github.smithjustinn.ui.game.components.timer.TimerDisplay
import io.github.smithjustinn.ui.game.components.timer.TimerFeedback
import io.github.smithjustinn.ui.game.components.timer.TimerLayout
import io.github.smithjustinn.ui.game.components.timer.TimerState
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun GameTopBar(
    state: GameTopBarState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMuteClick: () -> Unit,
    onScorePositioned: (Offset) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val spacing = PokerTheme.spacing

    GlassDashboard(
        modifier =
            modifier
                .statusBarsPadding()
                .padding(horizontal = spacing.medium, vertical = spacing.small),
        isHeatMode = state.isHeatMode,
    ) {
        val isTimeAttack = state.mode == GameMode.TIME_ATTACK
        val infiniteTransition = rememberInfiniteTransition(label = "TopBar")

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (state.compact) spacing.extraSmall else ROW_GAP_DP.dp),
        ) {
            // Bolt: Every child takes flat, stable parameters, so only the timer recomposes each second
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                BackButton(onClick = onBackClick, compact = state.compact)

                HudStats(
                    state = state,
                    infiniteTransition = infiniteTransition,
                    onScorePositioned = onScorePositioned,
                    modifier = Modifier.weight(1f),
                )

                MuteButton(
                    isAudioEnabled = state.isAudioEnabled,
                    onClick = onMuteClick,
                    compact = state.compact,
                )

                RestartButton(onClick = onRestartClick, compact = state.compact)
            }

            if (isTimeAttack && state.maxTime > 0) {
                TimeProgressBar(
                    time = state.time,
                    maxTime = state.maxTime,
                    isLowTime = state.isLowTime,
                    infiniteTransition = infiniteTransition,
                )
            }
        }
    }
}

@Composable
private fun HudStats(
    state: GameTopBarState,
    infiniteTransition: InfiniteTransition,
    onScorePositioned: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.padding(start = PokerTheme.spacing.extraSmall)) {
        // On a phone the stats share about 200dp, so they sit closer and use the denser numeral.
        val narrow = maxWidth < WIDE_STATS_MIN_WIDTH_DP.dp
        HudStatsRow(
            state = state,
            infiniteTransition = infiniteTransition,
            onScorePositioned = onScorePositioned,
            dense = state.compact || state.isHeatMode || narrow,
            // Four stats do not fit a phone; the combo pill below still shows the multiplier.
            showHeat = state.isHeatMode && !narrow,
        )
    }
}

@Composable
private fun HudStatsRow(
    state: GameTopBarState,
    infiniteTransition: InfiniteTransition,
    onScorePositioned: (Offset) -> Unit,
    dense: Boolean,
    showHeat: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (dense) STAT_GAP_DENSE_DP.dp else STAT_GAP_DP.dp),
    ) {
        TimerDisplay(
            infiniteTransition = infiniteTransition,
            state =
                TimerState(
                    time = state.time,
                    isLowTime = state.isLowTime,
                    isCriticalTime = state.isCriticalTime,
                ),
            feedback =
                TimerFeedback(
                    showTimeGain = state.showTimeGain,
                    timeGainAmount = state.timeGainAmount,
                    showTimeLoss = state.showTimeLoss,
                    timeLossAmount = state.timeLossAmount,
                    isMegaBonus = state.isMegaBonus,
                ),
            layout = if (dense) TimerLayout.COMPACT else TimerLayout.STANDARD,
        )

        if (!dense) {
            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(DIVIDER_HEIGHT_DP.dp)
                        .background(PokerTheme.colors.ivory.copy(alpha = TRACK_ALPHA)),
            )
        }

        HudStat(
            label = stringResource(Res.string.hud_pot_label),
            value = state.currentPot.toString(),
            dense = dense,
            modifier =
                remember(onScorePositioned) {
                    // Bolt: Prevent recreating Modifier.onGloballyPositioned on every recomposition
                    Modifier.onGloballyPositioned { coords ->
                        onScorePositioned(coords.positionInRoot())
                    }
                },
        )

        HudStat(
            label = stringResource(Res.string.hud_score_label),
            value = state.bankedScore.toString(),
            dense = dense,
            valueColor = PokerTheme.colors.ivory,
        )

        if (showHeat) {
            HudStat(
                label = stringResource(Res.string.hud_heat_label),
                value = stringResource(Res.string.combo_badge_format, state.comboMultiplier),
                dense = dense,
                labelColor = PokerTheme.colors.goldenYellow,
            )
        }
    }
}

@Composable
private fun HudStat(
    label: String,
    value: String,
    dense: Boolean,
    modifier: Modifier = Modifier,
    labelColor: Color = PokerTheme.colors.sage,
    valueColor: Color = PokerTheme.colors.goldenYellow,
) {
    Column(
        modifier =
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            },
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        HudStatLabel(text = label, color = labelColor)
        Text(
            text = value,
            style = hudNumeralStyle(dense = dense),
            color = valueColor,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HudIconButton(
    icon: ImageVector,
    contentDesc: String,
    tint: Color,
    onClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
    iconSize: Int = ICON_SIZE_DP,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { Text(contentDesc) },
        state = rememberTooltipState(),
    ) {
        Box(
            modifier =
                modifier
                    .size(if (compact) BUTTON_SIZE_COMPACT_DP.dp else BUTTON_SIZE_DP.dp)
                    .clip(PokerTheme.shapes.medium)
                    .pokerClickable(
                        onClick = onClick,
                        hapticType = HapticFeedbackType.LIGHT,
                        soundEffect = AudioService.SoundEffect.CLICK,
                    ).semantics { contentDescription = contentDesc },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(if (compact) ICON_SIZE_COMPACT_DP.dp else iconSize.dp),
            )
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    HudIconButton(
        icon = AppIcons.ChevronLeft,
        contentDesc = stringResource(Res.string.back_content_description),
        tint = PokerTheme.colors.ivory,
        onClick = onClick,
        compact = compact,
        modifier = modifier,
        iconSize = BACK_ICON_SIZE_DP,
    )
}

@Composable
private fun RestartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    HudIconButton(
        icon = AppIcons.Restart,
        contentDesc = stringResource(Res.string.restart_content_description),
        tint = PokerTheme.colors.goldenYellow,
        onClick = onClick,
        compact = compact,
        modifier = modifier,
    )
}

@Composable
private fun MuteButton(
    isAudioEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    HudIconButton(
        icon = if (isAudioEnabled) AppIcons.VolumeUp else AppIcons.VolumeOff,
        contentDesc =
            stringResource(
                if (isAudioEnabled) {
                    Res.string.mute_content_description
                } else {
                    Res.string.unmute_content_description
                },
            ),
        // The crossed-out glyph carries the muted state; sage just quietens it.
        tint = if (isAudioEnabled) PokerTheme.colors.goldenYellow else PokerTheme.colors.sage,
        onClick = onClick,
        compact = compact,
        modifier = modifier,
    )
}

@Composable
private fun TimeProgressBar(
    time: Long,
    maxTime: Long,
    isLowTime: Boolean,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
) {
    val progressState =
        animateFloatAsState(
            targetValue = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f),
            animationSpec = spring(stiffness = Spring.StiffnessLow),
        )

    val pulsingAlpha =
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(PULSE_DURATION, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "pulseAlpha",
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(PROGRESS_HEIGHT_DP.dp)
                .clip(CircleShape)
                .background(PokerTheme.colors.ivory.copy(alpha = TRACK_ALPHA)),
    ) {
        Box(
            modifier =
                Modifier
                    // Bolt: Use layout modifier to update width without recomposition during animation
                    .layout { measurable, constraints ->
                        val progress = progressState.value
                        val width = (constraints.maxWidth * progress).roundToInt()
                        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                        layout(width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    }.fillMaxHeight()
                    .graphicsLayer {
                        alpha = if (isLowTime) pulsingAlpha.value else 1f
                    }.clip(CircleShape)
                    .background(if (isLowTime) PokerTheme.colors.tacticalRed else PokerTheme.colors.goldenYellow),
        )
    }
}

private const val ROW_GAP_DP = 10
private const val STAT_GAP_DP = 18
private const val STAT_GAP_DENSE_DP = 12
private const val WIDE_STATS_MIN_WIDTH_DP = 300
private const val DIVIDER_HEIGHT_DP = 32
private const val BUTTON_SIZE_DP = 44
private const val BUTTON_SIZE_COMPACT_DP = 40
private const val ICON_SIZE_DP = 22
private const val BACK_ICON_SIZE_DP = 24
private const val ICON_SIZE_COMPACT_DP = 20
private const val PROGRESS_HEIGHT_DP = 4
private const val TRACK_ALPHA = 0.16f
private const val PULSE_DURATION = 800
