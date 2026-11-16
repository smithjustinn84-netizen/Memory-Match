package io.github.smithjustinn.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.assets.AssetProvider

/**
 * A titled row of card-shaped swatches for the card backs or symbol styles the player owns.
 * The row scrolls sideways, because the number of owned items grows with shop purchases.
 */
@Composable
internal fun CosmeticPicker(
    title: String,
    options: List<CosmeticOption>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PICKER_TITLE_GAP_DP.dp),
    ) {
        Text(
            text = title,
            style = PokerTheme.typography.titleSmall,
            color = PokerTheme.colors.ivory,
            modifier = Modifier.semantics { heading() },
        )

        LazyRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
        ) {
            items(options, key = { it.id }) { option ->
                CosmeticTile(option = option, onSelect = onSelect)
            }
        }
    }
}

@Composable
private fun CosmeticTile(
    option: CosmeticOption,
    onSelect: (String) -> Unit,
) {
    val colors = PokerTheme.colors
    val shape = PokerTheme.shapes.medium

    Column(
        modifier =
            Modifier
                .width(TILE_WIDTH_DP.dp)
                .clip(shape)
                .background(if (option.isSelected) colors.feltGreenDark else Color.Transparent)
                .border(
                    width = TILE_RING_WIDTH_DP.dp,
                    color = if (option.isSelected) colors.goldenYellow else Color.Transparent,
                    shape = shape,
                ).selectable(
                    selected = option.isSelected,
                    role = Role.RadioButton,
                    onClick = { if (!option.isSelected) onSelect(option.id) },
                ).semantics { contentDescription = option.name }
                .padding(vertical = PokerTheme.spacing.small, horizontal = PokerTheme.spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TILE_LABEL_GAP_DP.dp),
    ) {
        // Drawn at double size and scaled down, so the card's corners and details keep their proportions.
        Box(
            modifier = Modifier.size(width = SWATCH_WIDTH_DP.dp, height = SWATCH_HEIGHT_DP.dp).clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
        ) {
            AssetProvider.CardPreview(
                shopItemId = option.id,
                hexColor = option.hexColor,
                modifier =
                    Modifier
                        .requiredSize(
                            width = (SWATCH_WIDTH_DP * SWATCH_RENDER_SCALE).dp,
                            height = (SWATCH_HEIGHT_DP * SWATCH_RENDER_SCALE).dp,
                        ).graphicsLayer {
                            scaleX = 1f / SWATCH_RENDER_SCALE
                            scaleY = 1f / SWATCH_RENDER_SCALE
                        },
            )
        }

        Text(
            text = option.name,
            style = PokerTheme.typography.labelSmall,
            color = if (option.isSelected) colors.ivory else colors.sageLight,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

private const val PICKER_TITLE_GAP_DP = 10
private const val TILE_WIDTH_DP = 84
private const val TILE_RING_WIDTH_DP = 1.5f
private const val TILE_LABEL_GAP_DP = 6
private const val SWATCH_WIDTH_DP = 44
private const val SWATCH_HEIGHT_DP = 62
private const val SWATCH_RENDER_SCALE = 2.5f
