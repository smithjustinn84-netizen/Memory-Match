package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.combo_format
import org.jetbrains.compose.resources.stringResource

@Composable
fun ComboBadge(
    combo: Int,
    isMegaBonus: Boolean,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = combo > 1,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        val comboScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Surface(
            color = if (isMegaBonus) Color(0xFFFFD700) else MaterialTheme.colorScheme.tertiary,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .scale(comboScale)
                .shadow(4.dp, RoundedCornerShape(8.dp))
        ) {
            Text(
                text = stringResource(Res.string.combo_format, combo),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                ),
                color = if (isMegaBonus) Color.Black else MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}
