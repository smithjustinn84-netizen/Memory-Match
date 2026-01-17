package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.smithjustinn.domain.models.CardState

private data class CardLayoutInfo(val position: Offset, val size: Size)

@Composable
fun GameGrid(
    cards: List<CardState>,
    onCardClick: (Int) -> Unit,
    isPeeking: Boolean = false,
    lastMatchedIds: List<Int> = emptyList(),
    showComboExplosion: Boolean = false
) {
    val cardLayouts = remember { mutableStateMapOf<Int, CardLayoutInfo>() }
    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                gridPosition = layoutCoordinates.positionInRoot()
            }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(cards, key = { it.id }) { card ->
                PlayingCard(
                    suit = card.suit,
                    rank = card.rank,
                    isFaceUp = card.isFaceUp || isPeeking,
                    isMatched = card.isMatched,
                    isError = card.isError,
                    onClick = { onCardClick(card.id) },
                    modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                        cardLayouts[card.id] = CardLayoutInfo(
                            position = layoutCoordinates.positionInRoot(),
                            size = layoutCoordinates.size.toSize()
                        )
                    }
                )
            }
        }

        if (showComboExplosion && lastMatchedIds.isNotEmpty()) {
            val matchInfos = lastMatchedIds.mapNotNull { cardLayouts[it] }
            if (matchInfos.isNotEmpty()) {
                // Calculate the center of all matched cards
                val averageRootPosition = matchInfos
                    .fold(Offset.Zero) { acc, info -> 
                        acc + info.position + Offset(info.size.width / 2, info.size.height / 2)
                    }
                    .let { it / matchInfos.size.toFloat() }
                
                // Calculate position relative to the Box
                val relativeCenter = averageRootPosition - gridPosition

                ExplosionEffect(
                    modifier = Modifier.fillMaxSize(),
                    particleCount = 60,
                    centerOverride = relativeCenter
                )
            }
        }
    }
}
