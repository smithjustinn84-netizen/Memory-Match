package io.github.smithjustinn.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.PokerTheme

private const val TEXT_SHADOW_BLUR_RADIUS = 4f

@Composable
fun GlimmerText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    val glimmerTranslation = rememberGlimmerTranslation()
    val baseStyle =
        TextStyle(
            fontFamily = PokerTheme.fonts.display,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
        )

    Box(modifier = modifier) {
        // Shadow layer: a dark copy of the text, softened, sitting just behind the gold.
        Text(
            text = text,
            style =
                baseStyle.copy(
                    color = Color.Black.copy(alpha = 0.5f),
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset.Zero,
                            blurRadius = TEXT_SHADOW_BLUR_RADIUS,
                        ),
                ),
            modifier = Modifier.offset(x = 2.dp, y = 2.dp).clearAndSetSemantics { },
        )
        // Glimmer layer: the text is the mask, the gradient is painted over it while drawing,
        // so the sweep never recomposes or re-lays-out the text.
        Text(
            text = text,
            style = baseStyle.copy(color = Color.White),
            modifier =
                Modifier
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithCache { drawGlimmerOverContent(glimmerTranslation) },
        )
    }
}
