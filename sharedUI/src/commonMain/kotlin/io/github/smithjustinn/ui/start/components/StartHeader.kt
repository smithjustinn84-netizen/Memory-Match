package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.app_name
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

/**
 * StartHeader
 */
@Composable
fun StartHeader(
    modifier: Modifier = Modifier,
    backTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    symbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    areSuitsMultiColored: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CardPreview(
            modifier = Modifier.height(180.dp),
            backTheme = backTheme,
            symbolTheme = symbolTheme,
            areSuitsMultiColored = areSuitsMultiColored,
        )
        StartTitle(modifier = Modifier.padding(top = PokerTheme.spacing.medium))
    }
}

/** "Memory Match" in the display face, with the last word picked out in gold. */
@Composable
private fun StartTitle(modifier: Modifier = Modifier) {
    val name = stringResource(Res.string.app_name)
    val gold = PokerTheme.colors.goldenYellow
    val title =
        remember(name, gold) {
            val split = name.lastIndexOf(' ')
            buildAnnotatedString {
                append(name.substring(0, split + 1))
                withStyle(SpanStyle(color = gold)) { append(name.substring(split + 1)) }
            }
        }

    Text(
        text = title,
        style =
            PokerTheme.typography.displaySmall.copy(
                fontSize = TITLE_FONT_SP.sp,
                lineHeight = TITLE_LINE_HEIGHT_SP.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        color = PokerTheme.colors.ivory,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

private const val TITLE_FONT_SP = 44
private const val TITLE_LINE_HEIGHT_SP = 48
