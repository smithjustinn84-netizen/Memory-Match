package io.github.smithjustinn.ui.shop

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.assets.AssetProvider
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.components.ShopIcons
import io.github.smithjustinn.ui.components.pokerBackground

@Composable
fun ShopContent(
    component: ShopComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            component.onClearError()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pokerBackground(),
    ) {
        AuroraEffect(
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
        ) {
            ShopHeader(
                balance = state.balance,
                onBackClicked = { component.onBackClicked() },
            )

            ShopItemsGrid(
                state = state,
                onBuyItem = { component.onBuyItemClicked(it) },
                onEquipItem = { component.onEquipItemClicked(it) },
                modifier = Modifier.weight(1f),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ShopHeader(
    balance: Long,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.ArrowBack,
            contentDescription = "Back",
            tint = PokerTheme.colors.onBackground,
            modifier =
                Modifier
                    .size(32.dp)
                    .clickable { onBackClicked() },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "The Shop",
            style = MaterialTheme.typography.headlineMedium,
            color = PokerTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))

        // Bankroll Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ShopIcons.CasinoChip,
                contentDescription = "Bankroll",
                tint = PokerTheme.colors.goldenYellow,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$$balance",
                style = MaterialTheme.typography.titleMedium,
                color = PokerTheme.colors.goldenYellow,
            )
        }
    }
}

@Composable
private fun ShopItemsGrid(
    state: ShopState,
    onBuyItem: (ShopItem) -> Unit,
    onEquipItem: (ShopItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        items(state.items) { item ->
            val isEquipped =
                when (item.type) {
                    ShopItemType.THEME -> item.id == state.activeThemeId
                    ShopItemType.CARD_SKIN -> item.id == state.activeSkinId
                    else -> false
                }

            ShopItemCard(
                item = item,
                isUnlocked = state.unlockedItemIds.contains(item.id),
                isEquipped = isEquipped,
                canAfford = state.balance >= item.price,
                onBuy = { onBuyItem(item) },
                onEquip = { onEquipItem(item) },
            )
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canClick = (!isUnlocked && canAfford) || (isUnlocked && !isEquipped && !item.isConsumable)
    val onClick = if (isUnlocked) onEquip else onBuy

    AppCard(
        modifier = modifier.clickable(enabled = canClick, onClick = onClick),
        backgroundColor =
            getCardBackgroundColor(isEquipped, isUnlocked),
        border =
            if (isEquipped) {
                androidx.compose.foundation.BorderStroke(2.dp, PokerTheme.colors.goldenYellow)
            } else {
                null
            },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShopItemHeader(item.name, isEquipped)

            // Visual preview for themes and skins
            if (item.type == ShopItemType.THEME ||
                item.type == ShopItemType.CARD_SKIN
            ) {
                AssetProvider.CardPreview(
                    shopItemId = item.id,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                )
            }

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = PokerTheme.colors.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(8.dp))

            ShopItemStatusBadge(item, isUnlocked, isEquipped, canAfford)
        }
    }
}

@Composable
private fun getCardBackgroundColor(
    isEquipped: Boolean,
    isUnlocked: Boolean,
): Color =
    when {
        isEquipped -> PokerTheme.colors.surface.copy(alpha = 0.8f)
        isUnlocked -> PokerTheme.colors.surface.copy(alpha = 0.5f)
        else -> PokerTheme.colors.surface
    }

@Composable
private fun ShopItemHeader(
    name: String,
    isEquipped: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = PokerTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (isEquipped) {
            Icon(
                imageVector = ShopIcons.CheckCircle,
                contentDescription = "Equipped",
                tint = PokerTheme.colors.goldenYellow,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ShopItemStatusBadge(
    item: ShopItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
) {
    when {
        isEquipped -> {
            Text(
                text = "EQUIPPED",
                style = MaterialTheme.typography.labelLarge,
                color = PokerTheme.colors.goldenYellow,
                fontWeight = FontWeight.Bold,
            )
        }
        isUnlocked && !item.isConsumable -> {
            Text(
                text = "EQUIP",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Green,
                fontWeight = FontWeight.Bold,
            )
        }
        else -> {
            Text(
                text = "$${item.price}",
                style = MaterialTheme.typography.labelLarge,
                color = if (canAfford) PokerTheme.colors.goldenYellow else Color.Red,
            )
        }
    }
}
