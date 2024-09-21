package fr.outadoc.justchatting.shared.presentation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val TwitchIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "TwitchIcon",
        defaultWidth = 32.dp,
        defaultHeight = 32.dp,
        viewportWidth = 2400f,
        viewportHeight = 2800f,
    ).apply {
        path(
            fill = SolidColor(Color(0xFFFFFFFF)),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(500f, 0f)
            lineTo(0f, 500f)
            verticalLineToRelative(1800f)
            horizontalLineToRelative(600f)
            verticalLineToRelative(500f)
            lineToRelative(500f, -500f)
            horizontalLineToRelative(400f)
            lineToRelative(900f, -900f)
            verticalLineTo(0f)
            horizontalLineTo(500f)
            close()
            moveTo(2200f, 1300f)
            lineToRelative(-400f, 400f)
            horizontalLineToRelative(-400f)
            lineToRelative(-350f, 350f)
            verticalLineToRelative(-350f)
            horizontalLineTo(600f)
            verticalLineTo(200f)
            horizontalLineToRelative(1600f)
            verticalLineTo(1300f)
            close()
            moveTo(1700f, 550f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(600f)
            horizontalLineToRelative(-200f)
            close()
            moveTo(1150f, 550f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(600f)
            horizontalLineToRelative(-200f)
            close()
        }
    }.build()
}
