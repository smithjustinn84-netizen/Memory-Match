package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.best_score_label
import io.github.smithjustinn.resources.busted
import io.github.smithjustinn.resources.buy_in_back_to_lobby
import io.github.smithjustinn.resources.casino_header_title
import io.github.smithjustinn.resources.clean_number_format
import io.github.smithjustinn.resources.double_rewards
import io.github.smithjustinn.resources.double_rewards_ad_note
import io.github.smithjustinn.resources.game_complete
import io.github.smithjustinn.resources.game_over
import io.github.smithjustinn.resources.high_roller_suite
import io.github.smithjustinn.resources.moves_label
import io.github.smithjustinn.resources.play_again
import io.github.smithjustinn.resources.score_combo_bonus_label
import io.github.smithjustinn.resources.score_daily_challenge_bonus
import io.github.smithjustinn.resources.score_double_down
import io.github.smithjustinn.resources.score_match_points_label
import io.github.smithjustinn.resources.score_move_efficiency
import io.github.smithjustinn.resources.score_time_bonus
import io.github.smithjustinn.resources.share_receipt
import io.github.smithjustinn.resources.time_label
import io.github.smithjustinn.resources.times_up
import io.github.smithjustinn.resources.total_payout
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.extensions.pokerClickable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val INITIAL_SCALE = 0.8f
private const val COMPACT_HEIGHT_THRESHOLD_DP = 450

private const val RECEIPT_MAX_WIDTH_DP = 400
private const val RECEIPT_TILT_DEGREES = -1f
private const val RECEIPT_CORNER_DP = 6
private const val RECEIPT_ELEVATION_DP = 18
private const val RECEIPT_TOP_PADDING_DP = 28
private const val RECEIPT_SECTION_GAP_DP = 20
private const val RECEIPT_ROW_GAP_DP = 12
private const val HEADER_LINE_GAP_DP = 6
private const val ACTION_GAP_DP = 12

private val TitleFontSize = 44.sp
private val TitleLineHeight = 48.sp
private val TitleTracking = (-0.01).em
private val TotalLabelFontSize = 14.sp
private val TotalLabelTracking = 0.1.em
private val TotalValueFontSize = 44.sp

@Composable
fun ResultsCard(
    isWon: Boolean,
    isBusted: Boolean = false,
    highScore: Int,
    moves: Int,
    elapsedTimeSeconds: Long,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit = {},
    onDoubleRewards: () -> Unit = {},
    canDoubleRewards: Boolean = false,
    modifier: Modifier = Modifier,
    mode: GameMode = GameMode.TIME_ATTACK,
    onScoreTick: () -> Unit = {},
    onBackToLobby: (() -> Unit)? = null,
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val titleRes =
        when {
            isBusted -> Res.string.busted

            isWon -> Res.string.game_complete
            isTimeAttack -> Res.string.times_up
            else -> Res.string.game_over
        }

    val animatedScore = rememberAnimatedScore(scoreBreakdown.earnedCurrency, onScoreTick)
    val scale = rememberResultsScale()
    val roundedScore = remember(animatedScore) { derivedStateOf { animatedScore.value.roundToInt() } }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // Pin the actions to the bottom when there is room; otherwise scroll everything together.
        val pinActions = constraints.hasBoundedHeight && maxHeight >= COMPACT_HEIGHT_THRESHOLD_DP.dp

        ResultsLayout(
            pinActions = pinActions,
            receipt = {
                Receipt(scale = { scale.value }) {
                    CasinoHeader(titleRes, isWon)
                    ReceiptDivider()
                    PayoutSection(scoreBreakdown)
                    GameStatsRow(moves, elapsedTimeSeconds)
                    ReceiptDivider()
                    TotalPayout(
                        animatedScore = { roundedScore.value },
                        highScore = highScore,
                        isBust = isBusted || scoreBreakdown.earnedCurrency <= 0,
                    )
                }
            },
            actions = {
                ResultsActions(onPlayAgain, onShareReplay, onDoubleRewards, canDoubleRewards, onBackToLobby)
            },
        )
    }
}

@Composable
private fun ResultsLayout(
    pinActions: Boolean,
    receipt: @Composable () -> Unit,
    actions: @Composable () -> Unit,
) {
    val spacing = PokerTheme.spacing
    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier
                .widthIn(max = RECEIPT_MAX_WIDTH_DP.dp)
                .then(if (pinActions) Modifier.fillMaxHeight() else Modifier.verticalScroll(scrollState))
                .padding(horizontal = spacing.large),
    ) {
        // Vertical padding leaves room for the tilt, the torn edge and the drop shadow.
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(if (pinActions) Modifier.weight(1f).verticalScroll(scrollState) else Modifier)
                    .padding(top = spacing.large, bottom = spacing.extraLarge),
        ) {
            receipt()
        }
        actions()
    }
}

/** Tilted slip of card-stock paper with rounded top corners and a torn bottom edge. */
@Composable
private fun Receipt(
    scale: () -> Float,
    content: @Composable ColumnScope.() -> Unit,
) {
    val paperColor = PokerTheme.colors.cardStock
    val paperShape = RoundedCornerShape(topStart = RECEIPT_CORNER_DP.dp, topEnd = RECEIPT_CORNER_DP.dp)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val entranceScale = scale()
                    scaleX = entranceScale
                    scaleY = entranceScale
                    rotationZ = RECEIPT_TILT_DEGREES
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(elevation = RECEIPT_ELEVATION_DP.dp, shape = paperShape)
                    .background(paperColor)
                    .padding(horizontal = PokerTheme.spacing.large)
                    .padding(top = RECEIPT_TOP_PADDING_DP.dp, bottom = PokerTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(RECEIPT_SECTION_GAP_DP.dp),
            content = content,
        )
        ReceiptEdge(color = paperColor)
    }
}

@Composable
private fun CasinoHeader(
    titleRes: StringResource,
    isWon: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HEADER_LINE_GAP_DP.dp),
    ) {
        ReceiptCaption(
            text = stringResource(Res.string.casino_header_title),
            tracking = ReceiptHeaderTracking,
        )
        Text(
            text = stringResource(if (isWon) Res.string.game_complete else titleRes),
            style =
                TextStyle(
                    fontFamily = PokerTheme.fonts.display,
                    fontWeight = FontWeight.Bold,
                    fontSize = TitleFontSize,
                    lineHeight = TitleLineHeight,
                    letterSpacing = TitleTracking,
                ),
            color = PokerTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        ReceiptCaption(
            text = stringResource(Res.string.high_roller_suite),
            tracking = ReceiptHeaderTracking,
        )
    }
}

@Composable
private fun PayoutSection(scoreBreakdown: ScoreBreakdown) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RECEIPT_ROW_GAP_DP.dp),
    ) {
        PayoutRow(stringResource(Res.string.score_match_points_label), scoreBreakdown.basePoints)
        if (scoreBreakdown.comboBonus > 0) {
            PayoutRow(stringResource(Res.string.score_combo_bonus_label), scoreBreakdown.comboBonus)
        }
        if (scoreBreakdown.doubleDownBonus > 0) {
            PayoutRow(stringResource(Res.string.score_double_down), scoreBreakdown.doubleDownBonus)
        }
        PayoutRow(stringResource(Res.string.score_time_bonus), scoreBreakdown.timeBonus)
        PayoutRow(stringResource(Res.string.score_move_efficiency), scoreBreakdown.moveBonus)
        if (scoreBreakdown.dailyChallengeBonus > 0) {
            PayoutRow(stringResource(Res.string.score_daily_challenge_bonus), scoreBreakdown.dailyChallengeBonus)
        }
    }
}

@Composable
private fun GameStatsRow(
    moves: Int,
    elapsedTimeSeconds: Long,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ReceiptCaption(text = stringResource(Res.string.moves_label, moves))
        ReceiptCaption(text = stringResource(Res.string.time_label, formatTime(elapsedTimeSeconds)))
    }
}

@Composable
private fun TotalPayout(
    animatedScore: () -> Int,
    highScore: Int,
    isBust: Boolean,
) {
    val monoBold = TextStyle(fontFamily = PokerTheme.fonts.mono, fontWeight = FontWeight.Bold)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.extraSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RECEIPT_ROW_GAP_DP.dp),
        ) {
            Text(
                text = stringResource(Res.string.total_payout).uppercase(),
                style = monoBold.copy(fontSize = TotalLabelFontSize, letterSpacing = TotalLabelTracking),
                color = PokerTheme.colors.ink,
                modifier = Modifier.weight(1f).alignByBaseline(),
            )
            Text(
                text = stringResource(Res.string.clean_number_format, animatedScore()),
                style = monoBold.copy(fontSize = TotalValueFontSize, lineHeight = TotalValueFontSize),
                color = if (isBust) PokerTheme.colors.tacticalRed else PokerTheme.colors.ink,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.alignByBaseline(),
            )
        }
        ReceiptCaption(text = stringResource(Res.string.best_score_label, highScore))
    }
}

@Composable
private fun ResultsActions(
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
    onDoubleRewards: () -> Unit,
    canDoubleRewards: Boolean,
    onBackToLobby: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ACTION_GAP_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // PokerButton performs the haptic itself: heavy for primary, light otherwise.
        PokerButton(
            text = stringResource(Res.string.play_again),
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(),
            isPrimary = true,
        )

        if (canDoubleRewards) {
            DoubleRewardsAction(onDoubleRewards)
        }

        PokerButton(
            text = stringResource(Res.string.share_receipt),
            onClick = onShareReplay,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = AppIcons.Share,
            containerColor = Color.Transparent,
        )

        if (onBackToLobby != null) {
            Box(
                modifier =
                    Modifier
                        .height(LOBBY_LINK_HEIGHT_DP.dp)
                        .clip(PokerTheme.shapes.medium)
                        .pokerClickable(onClick = onBackToLobby, role = Role.Button)
                        .padding(horizontal = PokerTheme.spacing.medium),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.buy_in_back_to_lobby),
                    style = PokerTheme.typography.labelLarge,
                    color = PokerTheme.colors.sageLight,
                )
            }
        }
    }
}

private const val LOBBY_LINK_HEIGHT_DP = 44

@Composable
private fun DoubleRewardsAction(onDoubleRewards: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PokerButton(
            text = stringResource(Res.string.double_rewards),
            onClick = onDoubleRewards,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(Res.string.double_rewards_ad_note),
            style = PokerTheme.typography.bodySmall,
            color = PokerTheme.colors.sageLight,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun rememberResultsScale(): State<Float> {
    val scale = remember { Animatable(INITIAL_SCALE) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
        )
    }
    // Returned as a State so the spring is read in the receipt's layer block, not in composition.
    return remember(scale) { scale.asState() }
}
