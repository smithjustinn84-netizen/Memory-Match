package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.game_complete
import io.github.smithjustinn.resources.game_over
import io.github.smithjustinn.resources.play_again
import io.github.smithjustinn.resources.times_up
import io.github.smithjustinn.ui.components.AppIcons
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val HIGH_SCORE_THRESHOLD = 100
private const val HIGH_SCORE_STEP = 10
private const val LOW_SCORE_STEP = 1
private const val INITIAL_SCALE = 0.8f

// Receipt Colors
private val ReceiptPaperColor = Color(0xFFFDFBF7)
private val ReceiptInkColor = Color(0xFF2B2B2B)
private val ReceiptAccentColor = Color(0xFF8B0000) // Dark Red for key elements

@Composable
fun ResultsCard(
    isWon: Boolean,
    score: Int,
    moves: Int,
    elapsedTimeSeconds: Long,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit = {},
    modifier: Modifier = Modifier,
    mode: GameMode = GameMode.STANDARD,
    onScoreTick: () -> Unit = {},
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val titleRes =
        when {
            isWon -> Res.string.game_complete
            isTimeAttack -> Res.string.times_up
            else -> Res.string.game_over
        }

    val animatedScore = rememberAnimatedScore(score, onScoreTick)
    val scale = rememberResultsScale()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val isCompactHeight = maxHeight < 450.dp

        Box(
            modifier =
                Modifier
                    .scale(scale)
                    .widthIn(max = 400.dp) // Receipt width
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(2.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(ReceiptPaperColor),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth(),
            ) {
                // Zigzag Top
                ReceiptEdge(color = ReceiptPaperColor, isTop = true)

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 8.dp, bottom = 24.dp)
                            .then(if (isCompactHeight) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Casino Header
                    CasinoHeader(titleRes, isWon)

                    // Divider
                    ReceiptDivider()

                    // Detailed Payout List
                    PayoutSection(
                        scoreBreakdown = scoreBreakdown,
                        moves = moves,
                        elapsedTimeSeconds = elapsedTimeSeconds,
                    )

                    // Divider
                    ReceiptDivider()

                    // Total
                    TotalPayout(animatedScore.value.roundToInt())

                    // Barcode / Footer
                    ReceiptFooter(onPlayAgain, onShareReplay)
                }

                // Zigzag Bottom
                ReceiptEdge(color = ReceiptPaperColor, isTop = false)
            }
        }
    }
}

@Composable
private fun CasinoHeader(
    titleRes: org.jetbrains.compose.resources.StringResource,
    isWon: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = AppIcons.CasinoChip,
            contentDescription = null,
            tint = ReceiptInkColor,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "MEMORY MATCH CASINO",
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
            color = ReceiptInkColor.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(titleRes).uppercase(),
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                ),
            color = if (isWon) ReceiptAccentColor else ReceiptInkColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "HIGH ROLLER SUITE",
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                ),
            color = ReceiptInkColor.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun PayoutSection(
    scoreBreakdown: ScoreBreakdown,
    moves: Int,
    elapsedTimeSeconds: Long,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PayoutRow("Base Match Pts", scoreBreakdown.basePoints)
        if (scoreBreakdown.comboBonus > 0) {
            PayoutRow("Combo Bonus", scoreBreakdown.comboBonus)
        }
        if (scoreBreakdown.doubleDownBonus > 0) {
            PayoutRow("Double Or Nothing", scoreBreakdown.doubleDownBonus)
        }
        PayoutRow("Time Bonus", scoreBreakdown.timeBonus)
        PayoutRow("Move Efficiency", scoreBreakdown.moveBonus)

        ReceiptDottedLine()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "MOVES: $moves",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = ReceiptInkColor.copy(alpha = 0.7f),
            )
            Text(
                text = "TIME: ${formatTime(elapsedTimeSeconds)}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = ReceiptInkColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun PayoutRow(
    label: String,
    amount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                ),
            color = ReceiptInkColor,
        )
        Text(
            text = "+$amount",
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
            color = ReceiptInkColor,
        )
    }
}

@Composable
private fun TotalPayout(animatedScore: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "TOTAL PAYOUT",
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            color = ReceiptInkColor,
        )
        Text(
            text = "$animatedScore",
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                ),
            color = ReceiptAccentColor,
        )
    }
}

@Composable
private fun ReceiptFooter(
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Barcode Simulation
        Box(
            modifier =
                Modifier
                    .height(30.dp)
                    .fillMaxWidth(0.8f),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / 40
                var x = 0f
                while (x < size.width) {
                    val thickness = if ((x / barWidth).toInt() % 3 == 0) barWidth * 2 else barWidth / 2
                    if (kotlin.random.Random.nextBoolean()) {
                        drawRect(
                            color = ReceiptInkColor,
                            topLeft = Offset(x, 0f),
                            size = Size(thickness, size.height),
                        )
                    }
                    x += barWidth * 2.5f
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = ReceiptInkColor,
                    contentColor = Color.White,
                ),
            shape = RoundedCornerShape(2.dp), // Sharp corners for receipt feel
        ) {
            Text(
                text = stringResource(Res.string.play_again).uppercase(),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }

        OutlinedButton(
            onClick = onShareReplay,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = ReceiptInkColor,
                ),
            border = androidx.compose.foundation.BorderStroke(1.dp, ReceiptInkColor),
            shape = RoundedCornerShape(2.dp),
        ) {
            Text(
                text = "SHARE RECEIPT",
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }
    }
}

@Composable
private fun ReceiptDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 1.dp,
        color = ReceiptInkColor,
    )
}

@Composable
private fun ReceiptDottedLine() {
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(vertical = 8.dp),
    ) {
        drawLine(
            color = ReceiptInkColor.copy(alpha = 0.5f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            strokeWidth = 2.dp.toPx(),
        )
    }
}

@Composable
private fun ReceiptEdge(
    color: Color,
    isTop: Boolean,
) {
    val height = 12.dp
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .let { if (isTop) it.rotate(180f) else it },
    ) {
        val width = size.width
        val triangleCount = 20
        val triangleWidth = width / triangleCount
        val path =
            Path().apply {
                moveTo(0f, 0f)
                for (i in 0 until triangleCount) {
                    lineTo(i * triangleWidth + triangleWidth / 2, size.height)
                    lineTo((i + 1) * triangleWidth, 0f)
                }
                close()
            }
        drawPath(path, color = color)
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

@Composable
private fun rememberAnimatedScore(
    score: Int,
    onScoreTick: () -> Unit,
): androidx.compose.runtime.State<Float> {
    val animatedScore = remember { Animatable(0f) }
    var lastRoundedScore by remember { mutableStateOf(0) }
    val scoreTickHandler by rememberUpdatedState(onScoreTick)

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        ) {
            val currentRounded = value.roundToInt()
            val step = if (score > HIGH_SCORE_THRESHOLD) HIGH_SCORE_STEP else LOW_SCORE_STEP
            if (currentRounded != lastRoundedScore && (currentRounded % step == 0 || currentRounded == score)) {
                scoreTickHandler()
                lastRoundedScore = currentRounded
            }
        }
    }
    return remember(animatedScore) { derivedStateOf { animatedScore.value } }
}

@Composable
private fun rememberResultsScale(): Float {
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
    return scale.value
}
