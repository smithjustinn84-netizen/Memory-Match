package io.github.smithjustinn.ui.circuit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.buy_in_back_to_lobby
import io.github.smithjustinn.resources.buy_in_banked_label
import io.github.smithjustinn.resources.buy_in_bust_penalty_label
import io.github.smithjustinn.resources.buy_in_circuit_title
import io.github.smithjustinn.resources.buy_in_payout_label
import io.github.smithjustinn.resources.buy_in_selection_title
import io.github.smithjustinn.resources.buy_in_stage_label
import io.github.smithjustinn.resources.buy_in_start_round
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.PillSegmentedControl
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.components.pokerBackground
import org.jetbrains.compose.resources.stringResource

@Composable
fun BuyInContent(
    component: BuyInComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pokerBackground(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(PokerTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(PokerTheme.spacing.huge))

            BuyInHeader(state.stage.id, state.stage.name)

            Spacer(modifier = Modifier.weight(1f))

            // Banked Score Medallion
            MedallionScore(
                label = stringResource(Res.string.buy_in_banked_label),
                value = state.bankedScore,
                color = PokerTheme.colors.silver,
            )

            Spacer(modifier = Modifier.height(PokerTheme.spacing.huge))

            BuyInSelectionCard(
                state = state,
                onWagerSelected = component::onWagerSelected,
            )

            Spacer(modifier = Modifier.weight(1f))

            BuyInActionButtons(
                onStartRound = component::onStartRound,
                onBack = component::onBack,
            )
        }
    }
}

@Composable
private fun BuyInHeader(
    stageId: Int,
    stageName: String,
) {
    Text(
        text = stringResource(Res.string.buy_in_circuit_title),
        style = PokerTheme.typography.displaySmall,
        color = PokerTheme.colors.goldenYellow,
        textAlign = TextAlign.Center,
    )

    Text(
        text = stringResource(Res.string.buy_in_stage_label, stageId, stageName),
        style = PokerTheme.typography.headlineSmall,
        color = PokerTheme.colors.silver,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun BuyInSelectionCard(
    state: BuyInUIState,
    onWagerSelected: (Int) -> Unit,
) {
    AppCard(title = stringResource(Res.string.buy_in_selection_title)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
        ) {
            PillSegmentedControl(
                items = state.availableWagers,
                selectedItem = state.selectedWager,
                onItemSelected = onWagerSelected,
                labelProvider = { it.toString() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Risk/Reward Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                InfoItem(
                    label = stringResource(Res.string.buy_in_payout_label),
                    value = "${state.stage.potGrowthMultiplier}x",
                    color = PokerTheme.colors.bonusGreen,
                )
                InfoItem(
                    label = stringResource(Res.string.buy_in_bust_penalty_label),
                    value = "-${(state.stage.bustPenalty * 100).toInt()}%",
                    color = PokerTheme.colors.tacticalRed,
                )
            }
        }
    }
}

@Composable
private fun BuyInActionButtons(
    onStartRound: () -> Unit,
    onBack: () -> Unit,
) {
    PokerButton(
        text = stringResource(Res.string.buy_in_start_round),
        onClick = onStartRound,
        modifier = Modifier.fillMaxWidth(),
        isPrimary = true,
        applyGlimmer = true,
        isPulsing = true,
    )

    Spacer(modifier = Modifier.height(PokerTheme.spacing.medium))

    TextButton(onClick = onBack) {
        Text(
            text = stringResource(Res.string.buy_in_back_to_lobby),
            style = PokerTheme.typography.labelLarge,
            color = PokerTheme.colors.silver,
        )
    }
}

@Composable
private fun MedallionScore(
    label: String,
    value: Int,
    color: Color,
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.size(140.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = PokerTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                letterSpacing = 2.sp,
            )
            Text(
                text = value.toString(),
                style = PokerTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = color,
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = PokerTheme.typography.labelSmall,
            color = PokerTheme.colors.silver.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = PokerTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}
