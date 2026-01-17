package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.utils.formatTime

@Composable
fun TimerDisplay(
    time: Long,
    isLowTime: Boolean,
    isCriticalTime: Boolean,
    showTimeGain: Boolean,
    timeGainAmount: Int,
    isMegaBonus: Boolean,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier
) {
    val timerColor by animateColorAsState(
        targetValue = when {
            showTimeGain && isMegaBonus -> Color(0xFFFFD700)
            showTimeGain -> Color(0xFF4CAF50)
            isLowTime -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = if (showTimeGain) 100 else 500)
    )

    val timerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCriticalTime) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = if (isLowTime) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(time),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.scale(timerScale),
                color = timerColor
            )

            AnimatedVisibility(
                visible = showTimeGain,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                Text(
                    text = "+${timeGainAmount}s",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isMegaBonus) Color(0xFFFFD700) else Color(0xFF4CAF50),
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
