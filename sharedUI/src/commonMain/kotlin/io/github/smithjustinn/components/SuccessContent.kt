package io.github.smithjustinn.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.ScoreBreakdown
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.final_score
import memory_match.sharedui.generated.resources.play_again
import memory_match.sharedui.generated.resources.score_breakdown_title
import memory_match.sharedui.generated.resources.score_match_points
import memory_match.sharedui.generated.resources.score_move_bonus
import memory_match.sharedui.generated.resources.score_time_bonus
import memory_match.sharedui.generated.resources.success_emoji
import memory_match.sharedui.generated.resources.success_message
import memory_match.sharedui.generated.resources.success_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SuccessContent(
    onPlayAgain: () -> Unit,
    scoreBreakdown: ScoreBreakdown? = null,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(Res.string.success_emoji),
                fontSize = 100.sp,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.success_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.success_message),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            if (scoreBreakdown != null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = stringResource(Res.string.final_score, scoreBreakdown.totalScore),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.score_breakdown_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    ScoreRow(stringResource(Res.string.score_match_points, scoreBreakdown.matchPoints))
                    ScoreRow(stringResource(Res.string.score_time_bonus, scoreBreakdown.timeBonus), isBonus = true)
                    ScoreRow(stringResource(Res.string.score_move_bonus, scoreBreakdown.moveBonus), isBonus = true)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = stringResource(Res.string.play_again),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScoreRow(text: String, isBonus: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isBonus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
