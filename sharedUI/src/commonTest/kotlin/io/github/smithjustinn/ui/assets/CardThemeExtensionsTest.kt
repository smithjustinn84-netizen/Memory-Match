package io.github.smithjustinn.ui.assets

import androidx.compose.ui.graphics.Color
import io.github.smithjustinn.domain.models.CardBackTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class CardThemeExtensionsTest {
    @Test
    fun testGetPreferredColor() {
        assertEquals(Color(0xFF1C3F6E), CardBackTheme.GEOMETRIC.getPreferredColor())
        assertEquals(Color(0xFF7A1F2B), CardBackTheme.CLASSIC.getPreferredColor())
        assertEquals(Color(0xFF1A1A17), CardBackTheme.PATTERN.getPreferredColor())
        assertEquals(Color(0xFF7A1F2B), CardBackTheme.POKER.getPreferredColor())
    }

    @Test
    fun testToColor() {
        // Valid 6-digit hex
        assertEquals(Color(0xFFFF0000), "FF0000".toColor())
        assertEquals(Color(0xFF00FF00), "#00FF00".toColor())

        // Valid 8-digit hex (ARGB)
        assertEquals(Color(0x80FF0000), "80FF0000".toColor())
        assertEquals(Color(0x80FF0000), "#80FF0000".toColor())

        // Invalid hex
        assertEquals(Color.Gray, "invalid".toColor())
        assertEquals(Color.Gray, "GGGGGG".toColor())

        // Incorrect length
        assertEquals(Color.Gray, "F00".toColor())
        assertEquals(Color.Gray, "FF00000".toColor())
    }
}
