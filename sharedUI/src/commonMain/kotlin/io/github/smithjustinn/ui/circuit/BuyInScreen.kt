package io.github.smithjustinn.ui.circuit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BuyInScreen(
    component: BuyInComponent,
    modifier: Modifier = Modifier,
) {
    BuyInContent(
        component = component,
        modifier = modifier,
    )
}
