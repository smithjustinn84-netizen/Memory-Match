package io.github.smithjustinn.ui.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.GameModeOption
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GameModeOption(
            title = stringResource(Res.string.mode_standard),
            isSelected = selectedMode == GameMode.STANDARD,
            onClick = { onModeSelected(GameMode.STANDARD) },
            modifier = Modifier.weight(1f)
        )
        GameModeOption(
            title = stringResource(Res.string.mode_time_attack),
            isSelected = selectedMode == GameMode.TIME_ATTACK,
            onClick = { onModeSelected(GameMode.TIME_ATTACK) },
            modifier = Modifier.weight(1f)
        )
    }
}
