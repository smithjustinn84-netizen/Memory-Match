package io.github.smithjustinn.ui.shop.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.ui.assets.getPreferredColor
import io.github.smithjustinn.ui.assets.toColor
import io.github.smithjustinn.ui.game.components.cards.CardBack
import io.github.smithjustinn.ui.game.components.cards.CardFaces

object CosmeticPreviewRegistry {
    @Composable
    fun Preview(
        itemId: String,
        itemType: ShopItemType,
        hexColor: String? = null,
        modifier: Modifier = Modifier,
    ) {
        // Standard playing card aspect ratio
        val cardAspectRatio = 2.5f / 3.5f

        Box(
            modifier =
                modifier
                    .aspectRatio(cardAspectRatio)
                    .padding(4.dp), // Subtle padding for the card look
        ) {
            when (itemType) {
                ShopItemType.THEME -> ThemePreview(itemId, hexColor)
                ShopItemType.CARD_SKIN -> SkinPreview(itemId)
                else -> { /* Render generic icon for PowerUps/Currency */ }
            }
        }
    }

    @Composable
    private fun ThemePreview(
        themeId: String,
        hexColor: String?,
    ) {
        val theme = CardBackTheme.fromIdOrName(themeId)
        val color = hexColor?.toColor() ?: theme.getPreferredColor()

        CardBack(
            theme = theme,
            backColor = color,
            rotation = 0f,
        )
    }

    @Composable
    private fun SkinPreview(skinId: String) {
        // Map economy IDs to CardFace composables
        // We render a specific card (Ace of Spades) to demo the skin
        when (skinId) {
            "skin_classic" -> CardFaces.Classic()
            "skin_minimal" -> CardFaces.Minimal()
            "skin_text" -> CardFaces.TextOnly()
            "skin_poker" -> CardFaces.Poker()
            else -> CardFaces.Classic() // Fallback
        }
    }
}
