package io.github.smithjustinn.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.smithjustinn.ui.resources.Res
import io.github.smithjustinn.ui.resources.fraunces_bold
import io.github.smithjustinn.ui.resources.fraunces_semibold
import io.github.smithjustinn.ui.resources.inter_bold
import io.github.smithjustinn.ui.resources.inter_medium
import io.github.smithjustinn.ui.resources.inter_regular
import io.github.smithjustinn.ui.resources.inter_semibold
import io.github.smithjustinn.ui.resources.jetbrains_mono_bold
import io.github.smithjustinn.ui.resources.jetbrains_mono_medium
import org.jetbrains.compose.resources.Font

/**
 * The three type roles of the app: [display] for titles and card ranks, [body] for interface
 * text and [mono] for numerals (timer, pot, scores, receipt lines).
 *
 * The defaults are platform families, so anything rendered outside [AppTheme] still works.
 */
@Immutable
data class AppFonts(
    val display: FontFamily = FontFamily.Serif,
    val body: FontFamily = FontFamily.Default,
    val mono: FontFamily = FontFamily.Monospace,
)

/** Loads the bundled Fraunces, Inter and JetBrains Mono families. */
@Composable
internal fun rememberAppFonts(): AppFonts =
    AppFonts(
        display =
            FontFamily(
                Font(Res.font.fraunces_semibold, FontWeight.SemiBold),
                Font(Res.font.fraunces_bold, FontWeight.Bold),
            ),
        body =
            FontFamily(
                Font(Res.font.inter_regular, FontWeight.Normal),
                Font(Res.font.inter_medium, FontWeight.Medium),
                Font(Res.font.inter_semibold, FontWeight.SemiBold),
                Font(Res.font.inter_bold, FontWeight.Bold),
            ),
        mono =
            FontFamily(
                Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
                Font(Res.font.jetbrains_mono_bold, FontWeight.Bold),
            ),
    )

val LocalAppFonts = staticCompositionLocalOf { AppFonts() }
