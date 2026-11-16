package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.score_breakdown_title
import io.github.smithjustinn.resources.score_combo_bonus_label_caps
import io.github.smithjustinn.resources.score_double_down_caps
import io.github.smithjustinn.resources.score_match_points
import io.github.smithjustinn.resources.score_move_bonus
import io.github.smithjustinn.resources.score_time_bonus
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

private const val BREAKDOWN_ROW_GAP_DP = 12

/** Score breakdown as a small slip of receipt paper. */
@Composable
fun ScoreBreakdownSection(
    scoreBreakdown: ScoreBreakdown,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PokerTheme.colors.cardStock, PokerTheme.shapes.small)
                .padding(PokerTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(BREAKDOWN_ROW_GAP_DP.dp),
    ) {
        ReceiptCaption(
            text = stringResource(Res.string.score_breakdown_title),
            tracking = ReceiptHeaderTracking,
        )
        ReceiptDivider()

        ReceiptLineItem(
            label = stringResource(Res.string.score_match_points),
            value = scoreBreakdown.basePoints.toString(),
        )
        if (scoreBreakdown.comboBonus > 0) {
            ReceiptLineItem(
                label = stringResource(Res.string.score_combo_bonus_label_caps),
                value = "+${scoreBreakdown.comboBonus}",
            )
        }
        if (scoreBreakdown.doubleDownBonus > 0) {
            ReceiptLineItem(
                label = stringResource(Res.string.score_double_down_caps),
                value = "+${scoreBreakdown.doubleDownBonus}",
            )
        }
        ReceiptLineItem(
            label = stringResource(Res.string.score_time_bonus),
            value = if (scoreBreakdown.timeBonus > 0) "+${scoreBreakdown.timeBonus}" else "0",
        )
        ReceiptLineItem(
            label = stringResource(Res.string.score_move_bonus),
            value = if (scoreBreakdown.moveBonus > 0) "+${scoreBreakdown.moveBonus}" else "0",
        )
    }
}
