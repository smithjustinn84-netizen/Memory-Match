package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.receipt_plus_amount_format
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Muted ink for secondary receipt text. There is no theme token for it. */
private val ReceiptMutedInkColor = Color(0xFF5E5A4E)

/** Tracking for the small header lines at the top of the receipt. */
val ReceiptHeaderTracking = 0.14.em

/** Tracking for the small detail rows in the receipt body. */
val ReceiptDetailTracking = 0.06.em

private val ReceiptCaptionFontSize = 12.sp
private val ReceiptLineItemFontSize = 14.sp

private const val RULE_THICKNESS_DP = 1.5f
private const val RULE_DASH_DP = 5f
private const val RULE_GAP_DP = 4f
private const val RECEIPT_TOOTH_WIDTH_DP = 14
private const val RECEIPT_TOOTH_HEIGHT_DP = 7
private const val LINE_ITEM_GAP_DP = 12
private const val SECONDS_PER_MINUTE = 60
private const val SCORE_TICK_DURATION_MS = 1500
private const val HIGH_SCORE_THRESHOLD = 100
private const val HIGH_SCORE_STEP = 10
private const val LOW_SCORE_STEP = 1

/** Dashed ink rule that separates the sections of the receipt. */
@Composable
fun ReceiptDivider(
    modifier: Modifier = Modifier,
    color: Color = PokerTheme.colors.ink,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(RULE_THICKNESS_DP.dp),
    ) {
        val y = size.height / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            pathEffect =
                PathEffect.dashPathEffect(
                    floatArrayOf(RULE_DASH_DP.dp.toPx(), RULE_GAP_DP.dp.toPx()),
                    0f,
                ),
            strokeWidth = RULE_THICKNESS_DP.dp.toPx(),
        )
    }
}

/** Zig-zag torn edge that hangs below the bottom of the receipt paper. */
@Composable
fun ReceiptEdge(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(RECEIPT_TOOTH_HEIGHT_DP.dp),
    ) {
        val toothCount = ceil(size.width / RECEIPT_TOOTH_WIDTH_DP.dp.toPx()).toInt().coerceAtLeast(1)
        val toothWidth = size.width / toothCount
        // Start one pixel above the canvas so no seam shows between the paper and its teeth.
        val top = -1f
        val path =
            Path().apply {
                moveTo(0f, top)
                lineTo(0f, 0f)
                for (i in 0 until toothCount) {
                    lineTo(i * toothWidth + toothWidth / 2, size.height)
                    lineTo((i + 1) * toothWidth, 0f)
                }
                lineTo(size.width, top)
                close()
            }
        drawPath(path, color = color)
    }
}

/** Small uppercase mono line in muted ink: header lines, moves, time and best score. */
@Composable
fun ReceiptCaption(
    text: String,
    modifier: Modifier = Modifier,
    tracking: TextUnit = ReceiptDetailTracking,
) {
    Text(
        text = text.uppercase(),
        style =
            TextStyle(
                fontFamily = PokerTheme.fonts.mono,
                fontWeight = FontWeight.Medium,
                fontSize = ReceiptCaptionFontSize,
                letterSpacing = tracking,
            ),
        color = ReceiptMutedInkColor,
        modifier = modifier,
    )
}

fun formatTime(seconds: Long): String {
    val m = seconds / SECONDS_PER_MINUTE
    val s = seconds % SECONDS_PER_MINUTE
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

@Composable
fun rememberAnimatedScore(
    score: Int,
    onScoreTick: () -> Unit,
): State<Float> {
    val animatedScore = remember { Animatable(0f) }
    var lastRoundedScore by remember { mutableStateOf(0) }
    val scoreTickHandler by rememberUpdatedState(onScoreTick)

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = SCORE_TICK_DURATION_MS, easing = FastOutSlowInEasing),
        ) {
            handleScoreTick(value.roundToInt(), score, scoreTickHandler) { lastRoundedScore = it }
        }
    }
    return remember(animatedScore) { derivedStateOf { animatedScore.value } }
}

fun handleScoreTick(
    currentRounded: Int,
    targetScore: Int,
    scoreTickHandler: () -> Unit,
    updateLastScore: (Int) -> Unit,
) {
    val step = if (targetScore > HIGH_SCORE_THRESHOLD) HIGH_SCORE_STEP else LOW_SCORE_STEP
    if (currentRounded != 0 && (currentRounded % step == 0 || currentRounded == targetScore)) {
        scoreTickHandler()
        updateLastScore(currentRounded)
    }
}

/** Receipt line item: label on the left, "+amount" on the right. */
@Composable
fun PayoutRow(
    label: String,
    amount: Int,
    modifier: Modifier = Modifier,
    color: Color = PokerTheme.colors.ink,
) {
    ReceiptLineItem(
        label = label,
        value = stringResource(Res.string.receipt_plus_amount_format, amount),
        modifier = modifier,
        color = color,
    )
}

/** Receipt line item with a preformatted value. */
@Composable
fun ReceiptLineItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = PokerTheme.colors.ink,
) {
    val style =
        TextStyle(
            fontFamily = PokerTheme.fonts.mono,
            fontWeight = FontWeight.Medium,
            fontSize = ReceiptLineItemFontSize,
        )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(LINE_ITEM_GAP_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = style,
            color = color,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = style,
            color = color,
            maxLines = 1,
        )
    }
}
