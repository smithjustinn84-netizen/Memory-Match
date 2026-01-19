package io.github.smithjustinn.ui.difficulty.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

@Composable
fun DifficultyHeader(
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = if (scale < 1f) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            letterSpacing = if (scale < 1f) 1.sp else 2.sp
        )
    }
}
