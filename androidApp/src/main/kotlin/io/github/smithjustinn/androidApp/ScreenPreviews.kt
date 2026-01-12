package io.github.smithjustinn.androidApp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.smithjustinn.components.SuccessContent
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.theme.AppTheme

@Preview(showBackground = true)
@Composable
fun SuccessScreenPreview() {
    AppTheme {
        SuccessContent(
            onPlayAgain = {},
            scoreBreakdown = ScoreBreakdown(
                matchPoints = 320,
                timeBonus = 150,
                moveBonus = 1250,
                totalScore = 1720
            )
        )
    }
}
