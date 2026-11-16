package io.github.smithjustinn.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.smithjustinn.domain.models.CardTheme

private val LightColorScheme =
    lightColorScheme(
        primary = ModernGold,
        onPrimary = Color.Black,
        background = EmeraldGreenTop,
        onBackground = Color.White,
        surface = RaisedSurface,
        onSurface = ModernGold,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = ModernGold,
        onPrimary = Color.Black,
        background = EmeraldGreenDark,
        onBackground = Color.White,
        surface = RaisedSurfaceDark,
        onSurface = ModernGold,
    )

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }
internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// Dynamic, not static: only the readers recompose when a cosmetic is equipped, not the whole app.
internal val LocalCardTheme = compositionLocalOf { CardTheme() }

object PokerTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val spacing: AppSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSpacing.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val fonts: AppFonts
        @Composable
        @ReadOnlyComposable
        get() = LocalAppFonts.current

    val cardTheme: CardTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCardTheme.current
}

@Composable
fun AppTheme(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val systemIsDark = isSystemInDarkTheme()
    val isDarkState = remember(systemIsDark) { mutableStateOf(systemIsDark) }
    val colors = if (isDarkState.value) DarkAppColors else LightAppColors
    val spacing = AppSpacing()
    val shapes = AppShapes()
    val fonts = rememberAppFonts()
    val typography = remember(fonts) { appTypography(fonts) }

    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState,
        LocalAppColors provides colors,
        LocalAppSpacing provides spacing,
        LocalAppShapes provides shapes,
        LocalAppFonts provides fonts,
        LocalAppTypography provides typography,
    ) {
        val isDark by isDarkState
        onThemeChanged(isDark)
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography = typography,
            content = { Surface(content = content) },
        )
    }
}
