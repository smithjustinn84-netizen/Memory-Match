package io.github.smithjustinn.ui.shop.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.ui.game.components.cards.CardBacks
import io.github.smithjustinn.ui.game.components.cards.CardFaces

object CosmeticPreviewRegistry {
    @Composable
    fun Preview(
        itemId: String,
        itemType: ShopItemType,
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
                ShopItemType.THEME -> ThemePreview(itemId)
                ShopItemType.CARD_SKIN -> SkinPreview(itemId)
                else -> { /* Render generic icon for PowerUps/Currency */ }
            }
        }
    }

    @Composable
    private fun ThemePreview(themeId: String) {
        // Map economy IDs to CardBack composables
        when (themeId) {
            "theme_standard" -> CardBacks.Standard()
            "theme_classic" -> CardBacks.Classic()
            "theme_pattern" -> CardBacks.Pattern()
            "theme_poker" -> CardBacks.Poker()
            "theme_dark" -> CardBacks.Dark()
            "theme_nature" -> CardBacks.Nature()
            else -> CardBacks.Standard() // Fallback
        }
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
