package io.github.smithjustinn.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Display family for display, headline and large titles; body family for everything else.
 * Sizes and line heights stay on the Material 3 scale.
 */
internal fun appTypography(fonts: AppFonts): Typography {
    val base = Typography()

    fun TextStyle.title() = copy(fontFamily = fonts.display, fontWeight = FontWeight.SemiBold)

    fun TextStyle.body() = copy(fontFamily = fonts.body)

    fun TextStyle.label() = copy(fontFamily = fonts.body, fontWeight = FontWeight.SemiBold)

    return Typography(
        displayLarge = base.displayLarge.title(),
        displayMedium = base.displayMedium.title(),
        displaySmall = base.displaySmall.title(),
        headlineLarge = base.headlineLarge.title(),
        headlineMedium = base.headlineMedium.title(),
        headlineSmall = base.headlineSmall.title(),
        titleLarge = base.titleLarge.title(),
        titleMedium = base.titleMedium.body(),
        titleSmall = base.titleSmall.body(),
        bodyLarge = base.bodyLarge.body(),
        bodyMedium = base.bodyMedium.body(),
        bodySmall = base.bodySmall.body(),
        labelLarge = base.labelLarge.label(),
        labelMedium = base.labelMedium.label(),
        labelSmall = base.labelSmall.label(),
    )
}

val LocalAppTypography = staticCompositionLocalOf { appTypography(AppFonts()) }
