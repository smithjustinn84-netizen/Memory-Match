package io.github.smithjustinn.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class AppShapes(
    val small: Shape = RoundedCornerShape(6.dp),
    val medium: Shape = RoundedCornerShape(14.dp),
    val large: Shape = RoundedCornerShape(20.dp),
    val extraLarge: Shape = RoundedCornerShape(28.dp),
)

val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
