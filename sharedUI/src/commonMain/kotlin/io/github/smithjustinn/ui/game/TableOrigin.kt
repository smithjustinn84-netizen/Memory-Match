package io.github.smithjustinn.ui.game

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot

/**
 * The game table's own coordinate space, measured from a node inside the screen-shake layer.
 *
 * Root positions include the shake translation, so anything stored from them changed on every
 * frame of a shake and recomposed the whole grid. Positions taken relative to the table do not
 * move while it shakes.
 */
internal class TableOrigin {
    var coordinates: LayoutCoordinates? = null
}

internal val LocalTableOrigin = staticCompositionLocalOf<TableOrigin?> { null }

/** Position relative to the table, or to the root when there is no table (previews, tests). */
internal fun LayoutCoordinates.positionInTable(origin: TableOrigin?): Offset {
    val table = origin?.coordinates
    return if (table != null && table.isAttached) table.localPositionOf(this, Offset.Zero) else positionInRoot()
}
