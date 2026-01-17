package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.ui.components.AppIcons

@Composable
fun ScoreDisplay(
    score: Int,
    bestScore: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 6.dp,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedScoreText(score = score)

            if (bestScore > 0) {
                VerticalDivider(
                    modifier = Modifier.height(20.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                BestScoreBadge(bestScore = bestScore)
            }
        }
    }
}

@Composable
private fun AnimatedScoreText(
    score: Int,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = score,
        modifier = modifier,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut()
                )
            } else {
                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> height } + fadeOut()
                )
            }.using(SizeTransform(clip = false))
        }
    ) { targetScore ->
        Text(
            text = targetScore.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun BestScoreBadge(
    bestScore: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            AppIcons.Trophy,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFFFFD700)
        )
        Text(
            text = bestScore.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
    }
}
