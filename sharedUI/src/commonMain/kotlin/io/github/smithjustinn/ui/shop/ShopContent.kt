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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.pokerBackground
import io.github.smithjustinn.ui.components.AuroraEffect

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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = PokerTheme.colors.onBackground,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { component.onBackClicked() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "The Shop",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PokerTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Bankroll Display
                Row(
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.CasinoChip,
                        contentDescription = "Bankroll",
                        tint = PokerTheme.colors.goldenYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${state.balance}",
                        style = MaterialTheme.typography.titleMedium,
                        color = PokerTheme.colors.goldenYellow
                    )
                }
            }

            // Items Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.items) { item ->
                    ShopItemCard(
                        item = item,
                        isUnlocked = state.unlockedItemIds.contains(item.id),
                        canAfford = state.balance >= item.price,
                        onBuy = { component.onBuyItemClicked(item) }
                    )
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isUnlocked: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.clickable(enabled = !isUnlocked && canAfford, onClick = onBuy),
        backgroundColor = if (isUnlocked) PokerTheme.colors.surface.copy(alpha = 0.5f) else PokerTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = PokerTheme.colors.onSurface
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = PokerTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isUnlocked && !item.isConsumable) {
                Text(
                    text = "OWNED",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Green
                )
            } else {
                 Text(
                    text = "$${item.price}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canAfford) PokerTheme.colors.goldenYellow else Color.Red
                )
            }
        }
    }
}
