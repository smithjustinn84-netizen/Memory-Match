package io.github.smithjustinn.components.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    private var _arrowBack: ImageVector? = null
    val ArrowBack: ImageVector
        get() {
            if (_arrowBack != null) return _arrowBack!!
            _arrowBack = ImageVector.Builder(
                name = "ArrowBack",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(20.0f, 11.0f)
                    horizontalLineTo(7.83f)
                    lineTo(13.42f, 5.41f)
                    lineTo(12.0f, 4.0f)
                    lineTo(4.0f, 12.0f)
                    lineTo(12.0f, 20.0f)
                    lineTo(13.41f, 18.59f)
                    lineTo(7.83f, 13.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(11.0f)
                    close()
                }
            }.build()
            return _arrowBack!!
        }

    private var _info: ImageVector? = null
    val Info: ImageVector
        get() {
            if (_info != null) return _info!!
            _info = ImageVector.Builder(
                name = "Info",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                    reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(13.0f, 17.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineToRelative(-6.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(6.0f)
                    close()
                    moveTo(13.0f, 9.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineTo(7.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(2.0f)
                    close()
                }
            }.build()
            return _info!!
        }
}
