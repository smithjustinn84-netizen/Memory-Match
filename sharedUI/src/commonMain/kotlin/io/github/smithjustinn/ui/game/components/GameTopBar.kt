package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.AppIcons
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameTopBar(
    score: Int,
    time: Long,
    bestScore: Int,
    combo: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPeeking: Boolean = false,
    mode: GameMode = GameMode.STANDARD,
    maxTime: Long = 0,
    showTimeGain: Boolean = false,
    timeGainAmount: Int = 0,
    isMegaBonus: Boolean = false
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val isLowTime = isTimeAttack && time <= 10
    val isCriticalTime = isTimeAttack && time <= 5

    val infiniteTransition = rememberInfiniteTransition()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onBackClick)

            TimerDisplay(
                time = time,
                isLowTime = isLowTime,
                isCriticalTime = isCriticalTime,
                showTimeGain = showTimeGain,
                timeGainAmount = timeGainAmount,
                isMegaBonus = isMegaBonus,
                infiniteTransition = infiniteTransition
            )

            ScoreDisplay(
                score = score,
                bestScore = bestScore
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ComboBadge(
                combo = combo,
                isMegaBonus = isMegaBonus,
                infiniteTransition = infiniteTransition
            )

            PeekIndicator(isVisible = isPeeking)
        }

        if (isTimeAttack && maxTime > 0) {
            TimeProgressBar(
                time = time,
                maxTime = maxTime,
                isLowTime = isLowTime
            )
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.back_content_description),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PeekIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally(),
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.Visibility,
                    contentDescription = stringResource(Res.string.peek_cards),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeProgressBar(
    time: Long,
    maxTime: Long,
    isLowTime: Boolean,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        if (isLowTime) {
                            listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
                        } else {
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        }
                    )
                )
        )
    }
}
