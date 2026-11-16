package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.pairs_format
import io.github.smithjustinn.resources.resume_game
import io.github.smithjustinn.resources.select_difficulty
import io.github.smithjustinn.resources.start
import io.github.smithjustinn.theme.ChipBlue
import io.github.smithjustinn.theme.ChipGreen
import io.github.smithjustinn.theme.Ink
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.components.PokerChip
import io.github.smithjustinn.ui.start.DifficultyState
import io.github.smithjustinn.ui.start.displayNameRes
import io.github.smithjustinn.ui.stats.components.ModeSelector
import org.jetbrains.compose.resources.stringResource

/**
 * DifficultySelectionSection
 */
@Composable
fun DifficultySelectionSection(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.large),
    ) {
        DifficultySelector(state, onDifficultySelected)

        ModeSelector(
            selectedMode = state.selectedMode,
            onModeSelected = onModeSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        PrimaryActionButtons(
            state = state,
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
        )
    }
}

@Composable
private fun DifficultySelector(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.select_difficulty).uppercase(),
                style = PokerTheme.typography.labelMedium.copy(letterSpacing = SECTION_LETTER_SPACING_EM.em),
                color = PokerTheme.colors.sage,
            )

            val selected = state.selectedDifficulty
            Text(
                text =
                    stringResource(selected.type.displayNameRes) +
                        SUMMARY_SEPARATOR +
                        stringResource(Res.string.pairs_format, selected.pairs),
                style = PokerTheme.typography.labelLarge,
                color = PokerTheme.colors.ivory,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
        ) {
            state.difficulties.forEach { level ->
                DifficultyItem(
                    level = level,
                    isSelected = state.selectedDifficulty == level,
                    onSelected = onDifficultySelected,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DifficultyItem(
    level: DifficultyLevel,
    isSelected: Boolean,
    onSelected: (DifficultyLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PokerTheme.colors
    val chipColor =
        when (level.pairs) {
            PAIRS_EASY -> ChipBlue
            PAIRS_MEDIUM -> colors.tacticalRed
            PAIRS_HARD -> ChipGreen
            else -> Ink
        }
    val tileShape = PokerTheme.shapes.medium

    Column(
        modifier =
            modifier
                .clip(tileShape)
                .background(if (isSelected) colors.oakWood.copy(alpha = TILE_FILL_ALPHA) else Color.Transparent)
                .border(
                    width = TILE_RING_WIDTH_DP.dp,
                    color = if (isSelected) colors.goldenYellow else Color.Transparent,
                    shape = tileShape,
                ).padding(top = PokerTheme.spacing.small, bottom = TILE_BOTTOM_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
    ) {
        val difficultyName = stringResource(level.type.displayNameRes)
        val pairsDescription = stringResource(Res.string.pairs_format, level.pairs)
        val description = "$pairsDescription, $difficultyName"

        PokerChip(
            text = level.pairs.toString(),
            contentColor = chipColor,
            isSelected = isSelected,
            onClick = { onSelected(level) },
            contentDescription = description,
        )

        Text(
            text = difficultyName,
            style = PokerTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = PokerTheme.spacing.extraSmall).clearAndSetSemantics { },
            color = if (isSelected) colors.ivory else colors.sageLight,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun PrimaryActionButtons(
    state: DifficultyState,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
        modifier = Modifier.padding(top = PokerTheme.spacing.small),
    ) {
        val hasSavedGame = state.hasSavedGame

        if (hasSavedGame) {
            PokerButton(
                text = stringResource(Res.string.resume_game),
                onClick = onResumeGame,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true,
            )

            PokerButton(
                text = stringResource(Res.string.start),
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false,
            )
        } else {
            PokerButton(
                text = stringResource(Res.string.start),
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true,
            )
        }
    }
}

private const val SECTION_LETTER_SPACING_EM = 0.08f
private const val SUMMARY_SEPARATOR = " \u00B7 "
private const val TILE_FILL_ALPHA = 0.6f
private const val TILE_RING_WIDTH_DP = 1.5f
private const val TILE_BOTTOM_PADDING_DP = 10
private const val PAIRS_EASY = 6
private const val PAIRS_MEDIUM = 8
private const val PAIRS_HARD = 10
