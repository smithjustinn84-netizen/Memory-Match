package io.github.smithjustinn.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.splash_dealing_cards
import io.github.smithjustinn.resources.splash_title
import io.github.smithjustinn.theme.Brass
import io.github.smithjustinn.theme.Bronze
import io.github.smithjustinn.theme.FeltGreenBottom
import io.github.smithjustinn.theme.FeltGreenTop
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.ui.start.components.CardPreview
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private const val SPLASH_DELAY_MS = 2500L
private const val SPLASH_FADE_DURATION_MS = 1200
private const val SHIMMER_DURATION_MS = 2000
private const val SHIMMER_MAX_OFFSET = 2000f
private const val SHIMMER_START_OFFSET = 1000f
private const val TEXT_SHADOW_OFFSET_PX = 2f
private const val TEXT_SHADOW_BLUR_RADIUS = 4f

@Composable
fun SplashScreen(onDataLoaded: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    // Fix for ktlint(compose:lambda-param-in-effect)
    val currentOnDataLoaded by rememberUpdatedState(onDataLoaded)

    LaunchedEffect(Unit) {
        isVisible = true
        delay(SPLASH_DELAY_MS) // Slightly longer to appreciate the "premium" feel
        currentOnDataLoaded()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(FeltGreenTop, FeltGreenBottom),
                            center = Offset.Unspecified, // Defaults to center
                            radius = Float.POSITIVE_INFINITY, // Fill nicely
                        ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(SPLASH_FADE_DURATION_MS)),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Main Title with Golden Glimmer
                GlimmerText(stringResource(Res.string.splash_title), fontSize = 48.sp)

                Spacer(modifier = Modifier.height(32.dp))

                // Animated Card Preview
                CardPreview(
                    settings = CardDisplaySettings(),
                    modifier = Modifier.width(280.dp), // Slightly smaller to look focused
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Subtle Loading Text
                Text(
                    text = stringResource(Res.string.splash_dealing_cards),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color =
                                GoldenYellow
                                    .copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                        ),
                )
            }
        }
    }
}

@Composable
fun GlimmerText(
    text: String,
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
    Box {
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
