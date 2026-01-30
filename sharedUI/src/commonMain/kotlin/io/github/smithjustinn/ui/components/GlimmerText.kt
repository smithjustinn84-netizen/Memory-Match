package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.Brass
import io.github.smithjustinn.theme.Bronze
import io.github.smithjustinn.theme.GoldenYellow

private const val SHIMMER_DURATION_MS = 2000
private const val SHIMMER_MAX_OFFSET = 2000f
private const val SHIMMER_START_OFFSET = 1000f
private const val TEXT_SHADOW_OFFSET_PX = 2f
private const val TEXT_SHADOW_BLUR_RADIUS = 4f

@Composable
fun GlimmerText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
) {
    val shimmerColors =
        listOf(
            Bronze,
            GoldenYellow,
            Bronze,
            Brass,
            Bronze,
            GoldenYellow,
            Bronze,
            Brass,
            Bronze,
        )

    val transition = rememberInfiniteTransition(label = "glimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_MAX_OFFSET,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "glimmer_translate",
    )

    val brush =
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim - SHIMMER_START_OFFSET, translateAnim - SHIMMER_START_OFFSET),
            end = Offset(translateAnim, translateAnim),
            tileMode = TileMode.Clamp,
        )

    // Shadow Layer
    Box(modifier = modifier) {
        Text(
            text = text,
            style =
                TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    color = Color.Black.copy(alpha = 0.5f),
                ),
            modifier = Modifier.offset(x = 2.dp, y = 2.dp),
        )
        // Main Glimmer Layer
        Text(
            text = text,
            style =
                TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    brush = brush,
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(TEXT_SHADOW_OFFSET_PX, TEXT_SHADOW_OFFSET_PX),
                            blurRadius = TEXT_SHADOW_BLUR_RADIUS,
                        ),
                ),
        )
    }
}
