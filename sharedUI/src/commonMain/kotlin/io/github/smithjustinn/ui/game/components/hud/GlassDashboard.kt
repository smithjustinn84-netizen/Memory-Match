package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.PokerTheme

private const val PANEL_BORDER_ALPHA = 0.22f
private const val PANEL_BORDER_ALPHA_HEAT = 0.32f
private const val STAT_LABEL_FONT_SIZE_SP = 11
private const val STAT_LABEL_TRACKING_EM = 0.08f
private const val STAT_NUMERAL_FONT_SIZE_SP = 22
private const val STAT_NUMERAL_FONT_SIZE_DENSE_SP = 20
private const val STAT_NUMERAL_LINE_HEIGHT_SP = 26

/**
 * The rounded glass panel shared by the game HUD: the top bar and the bottom progress panel.
 * Outer margins are the caller's job; the panel only draws itself and pads its content.
 */
@Composable
fun GlassDashboard(
    modifier: Modifier = Modifier,
    isHeatMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = PokerTheme.colors
    val shape = PokerTheme.shapes.large
    val borderAlpha = if (isHeatMode) PANEL_BORDER_ALPHA_HEAT else PANEL_BORDER_ALPHA

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.hudBackground, shape)
                .border(1.dp, colors.goldenYellow.copy(alpha = borderAlpha), shape)
                .padding(PokerTheme.spacing.small),
    ) {
        content()
    }
}

/** Small uppercase caption above a HUD numeral ("TIME", "POT"). */
@Composable
internal fun HudStatLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PokerTheme.colors.sage,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        maxLines = 1,
        softWrap = false,
        style =
            PokerTheme.typography.labelSmall.copy(
                fontSize = STAT_LABEL_FONT_SIZE_SP.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = STAT_LABEL_TRACKING_EM.em,
            ),
    )
}

/** Mono numeral style for HUD stats; [dense] is for the three-stat heat row and compact layouts. */
@Composable
internal fun hudNumeralStyle(dense: Boolean = false): TextStyle =
    PokerTheme.typography.titleLarge.copy(
        fontFamily = PokerTheme.fonts.mono,
        fontWeight = FontWeight.Bold,
        fontSize = (if (dense) STAT_NUMERAL_FONT_SIZE_DENSE_SP else STAT_NUMERAL_FONT_SIZE_SP).sp,
        lineHeight = STAT_NUMERAL_LINE_HEIGHT_SP.sp,
        letterSpacing = 0.sp,
    )
