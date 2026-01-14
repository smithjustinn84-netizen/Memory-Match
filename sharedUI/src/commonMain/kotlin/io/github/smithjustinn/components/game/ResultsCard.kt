package io.github.smithjustinn.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.utils.formatTime
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.final_score
import memory_match.sharedui.generated.resources.game_complete
import memory_match.sharedui.generated.resources.game_over
import memory_match.sharedui.generated.resources.moves_label
import memory_match.sharedui.generated.resources.play_again
import memory_match.sharedui.generated.resources.score_breakdown_title
import memory_match.sharedui.generated.resources.score_match_points
import memory_match.sharedui.generated.resources.score_move_bonus
import memory_match.sharedui.generated.resources.score_time_bonus
import memory_match.sharedui.generated.resources.time_label
import memory_match.sharedui.generated.resources.times_up
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultsCard(
    isWon: Boolean,
    score: Int,
    moves: Int,
    elapsedTimeSeconds: Long,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
    mode: GameMode = GameMode.STANDARD
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val titleRes = when {
        isWon -> Res.string.game_complete
        isTimeAttack -> Res.string.times_up
        else -> Res.string.game_over
    }

    Card(
        modifier = modifier.padding(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWon) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isWon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.final_score, score),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isWon) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(Res.string.time_label, formatTime(elapsedTimeSeconds)),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isWon) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(Res.string.moves_label, moves),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isWon) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = if (isWon) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.score_breakdown_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isWon) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(Res.string.score_match_points, scoreBreakdown.matchPoints),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isWon) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(Res.string.score_time_bonus, scoreBreakdown.timeBonus),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isWon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(Res.string.score_move_bonus, scoreBreakdown.moveBonus),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isWon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(),
                colors = if (isWon) {
                    ButtonDefaults.buttonColors()
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                }
            ) {
                Text(stringResource(Res.string.play_again), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
