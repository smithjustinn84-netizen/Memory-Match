package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.app_name
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.GlimmerText
import org.jetbrains.compose.resources.stringResource

/**
 * StartHeader
 */
@Composable
fun StartHeader(
    modifier: Modifier = Modifier,
    settings: CardDisplaySettings = CardDisplaySettings(),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // App Title
        GlimmerText(
            text = stringResource(Res.string.app_name),
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = PokerTheme.spacing.medium),
        )

        CardPreview(
            modifier = Modifier.height(180.dp),
            settings = settings,
        )
    }
}
