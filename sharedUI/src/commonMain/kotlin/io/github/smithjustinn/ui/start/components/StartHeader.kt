package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.NeonCyan
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

/**
 * GameTitleHeader (Header Section - 2026 Design)
 * 
 * Contains the "Memory Match" title with neon glow and the tilted cards preview.
 * Refined with increased spacing for an "airy" feel.
 */
@Composable
fun StartHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val title = stringResource(Res.string.app_name)
        
        Text(
            text = title,
            style = TextStyle(
                fontSize = 48.sp, // Slightly larger for impact
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = NeonCyan.copy(alpha = 0.8f),
                    offset = Offset(0f, 0f),
                    blurRadius = 40f // Increased glow
                )
            )
        )

        Spacer(modifier = Modifier.height(40.dp)) // Increased from 24.dp for Step 6 polish

        CardPreview(
            modifier = Modifier.height(200.dp) // Slightly taller
        )
    }
}
