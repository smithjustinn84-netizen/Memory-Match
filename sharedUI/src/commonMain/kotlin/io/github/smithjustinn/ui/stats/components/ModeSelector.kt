package io.github.smithjustinn.ui.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.game_mode
import io.github.smithjustinn.resources.mode_daily_challenge_caps
import io.github.smithjustinn.resources.mode_time_attack_caps
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.PillSegmentedControl
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = listOf(GameMode.TIME_ATTACK, GameMode.DAILY_CHALLENGE)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LABEL_GAP_DP.dp),
    ) {
        Text(
            text = stringResource(Res.string.game_mode).uppercase(),
            style = PokerTheme.typography.labelMedium.copy(letterSpacing = LABEL_LETTER_SPACING_EM.em),
            color = PokerTheme.colors.sage,
        )

        PillSegmentedControl(
            items = modes,
            selectedItem = selectedMode,
            onItemSelected = onModeSelected,
            labelProvider = { mode ->
                when (mode) {
                    GameMode.TIME_ATTACK -> stringResource(Res.string.mode_time_attack_caps)
                    GameMode.DAILY_CHALLENGE -> stringResource(Res.string.mode_daily_challenge_caps)
                }
            },
        )
    }
}

private const val LABEL_GAP_DP = 12
private const val LABEL_LETTER_SPACING_EM = 0.08f
