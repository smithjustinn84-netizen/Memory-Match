package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.combo_badge_format
import io.github.smithjustinn.resources.hud_combo_label
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

private const val COMBO_ANIMATION_DURATION_MS = 400
private const val PULSE_SCALE_HEAT_COMPACT = 1.08f
private const val PULSE_SCALE_HEAT = 1.15f
private const val PULSE_SCALE_DEFAULT_COMPACT = 1.05f
private const val PULSE_SCALE_DEFAULT = 1.1f

private const val PILL_HEIGHT_COMPACT = 28
private const val PILL_HEIGHT_NORMAL = 36
private const val PILL_HORIZONTAL_PADDING = 12
private const val PILL_BORDER_ALPHA = 0.4f
private const val LABEL_FONT_SIZE_COMPACT = 10
private const val LABEL_FONT_SIZE_NORMAL = 12
private const val LABEL_TRACKING_EM = 0.06f
private const val VALUE_FONT_SIZE_COMPACT = 14
private const val VALUE_FONT_SIZE_NORMAL = 16

@Composable
fun ComboBadge(
    state: ComboBadgeState,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    AnimatedVisibility(
        visible = state.combo > 1,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        val comboPulseScaleState =
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue =
                    when {
                        state.isHeatMode -> if (compact) PULSE_SCALE_HEAT_COMPACT else PULSE_SCALE_HEAT
                        else -> if (compact) PULSE_SCALE_DEFAULT_COMPACT else PULSE_SCALE_DEFAULT
                    },
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(COMBO_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "comboPulse",
            )

        val popScale = remember { Animatable(1f) }

        LaunchedEffect(state.combo) {
            if (state.combo > 1) {
                popScale.snapTo(1.5f)
                popScale.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                )
            } else {
                popScale.snapTo(1f)
            }
        }

        val popScaleState = remember { derivedStateOf { popScale.value } }

        ComboBadgeContent(
            state = state,
            compact = compact,
            pulseScale = comboPulseScaleState,
            popScale = popScaleState,
        )
    }
}

@Composable
@Suppress("ktlint:compose:state-param-check")
private fun ComboBadgeContent(
    state: ComboBadgeState,
    compact: Boolean,
    pulseScale: State<Float>,
    popScale: State<Float>,
    modifier: Modifier = Modifier,
) {
    val colors = PokerTheme.colors

    Row(
        modifier =
            modifier
                .graphicsLayer {
                    // Bolt: Defer state read to graphicsLayer to skip recomposition on every animation frame
                    val scale = pulseScale.value * popScale.value
                    scaleX = scale
                    scaleY = scale
                }.height(if (compact) PILL_HEIGHT_COMPACT.dp else PILL_HEIGHT_NORMAL.dp)
                .background(colors.raisedSurface, CircleShape)
                .border(1.dp, colors.goldenYellow.copy(alpha = PILL_BORDER_ALPHA), CircleShape)
                .padding(horizontal = PILL_HORIZONTAL_PADDING.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
    ) {
        Text(
            text = stringResource(Res.string.hud_combo_label),
            style =
                PokerTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) LABEL_FONT_SIZE_COMPACT.sp else LABEL_FONT_SIZE_NORMAL.sp,
                    letterSpacing = LABEL_TRACKING_EM.em,
                ),
            color = colors.sage,
            maxLines = 1,
        )

        Text(
            text = stringResource(Res.string.combo_badge_format, state.combo),
            style =
                PokerTheme.typography.labelLarge.copy(
                    fontFamily = PokerTheme.fonts.mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) VALUE_FONT_SIZE_COMPACT.sp else VALUE_FONT_SIZE_NORMAL.sp,
                ),
            color = colors.goldenYellow,
            maxLines = 1,
        )
    }
}
