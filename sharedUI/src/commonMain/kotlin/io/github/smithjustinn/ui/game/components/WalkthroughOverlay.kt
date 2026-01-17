package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WalkthroughOverlay(
    step: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {} // Consume clicks
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getTitle(step),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = getDescription(step),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Skip")
                }
                
                Button(
                    onClick = {
                        if (step < 2) onNext() else onDismiss()
                    }
                ) {
                    Text(if (step < 2) "Next" else "Got it!")
                }
            }
        }
    }
}

private fun getTitle(step: Int): String = when (step) {
    0 -> "Welcome to Memory Match!"
    1 -> "Find Pairs"
    2 -> "Combos & Bonuses"
    else -> ""
}

private fun getDescription(step: Int): String = when (step) {
    0 -> "Test your memory by finding matching pairs of cards. Flip two cards at a time to see if they match!"
    1 -> "When you find a match, the cards stay face up. Match all pairs to win the game."
    2 -> "Match pairs quickly to build a combo multiplier and earn more points. In Time Attack mode, matches also give you extra time!"
    else -> ""
}
