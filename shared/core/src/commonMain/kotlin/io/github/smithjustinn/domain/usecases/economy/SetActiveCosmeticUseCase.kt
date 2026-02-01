package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository

class SetActiveCosmeticUseCase(
    private val playerEconomyRepository: PlayerEconomyRepository,
) {
    suspend operator fun invoke(
        itemId: String,
        itemType: ShopItemType,
    ) {
        when (itemType) {
            ShopItemType.THEME -> playerEconomyRepository.selectTheme(itemId)
            ShopItemType.CARD_SKIN -> playerEconomyRepository.selectSkin(itemId)
            else -> {
                // Other types like MUSIC or POWER_UP might have their own logic later
                // For now, these are the primary cosmetics
            }
        }
    }
}
