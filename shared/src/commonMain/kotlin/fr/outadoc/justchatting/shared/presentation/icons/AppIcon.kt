package fr.outadoc.justchatting.shared.presentation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val AppIcon: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "AppIcon",
            defaultWidth = 64.dp,
            defaultHeight = 64.dp,
            viewportWidth = 14.63f,
            viewportHeight = 14.63f,
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
                moveTo(7.566f, 0f)
                curveTo(6.5790f, 00f, 5.660f, 0.1850f, 4.8090f, 0.5550f)
                curveToRelative(-0.8510f, 0.370f, -1.60f, 0.8760f, -2.2470f, 1.5170f)
                curveTo(1.9150f, 2.7130f, 1.4090f, 3.4630f, 1.0450f, 4.320f)
                curveTo(0.6810f, 5.1770f, 0.4990f, 6.0920f, 0.4990f, 7.0670f)
                curveToRelative(00f, 0.580f, 0.0580f, 1.1440f, 0.1760f, 1.6930f)
                curveToRelative(0.1170f, 0.5490f, 0.2990f, 1.0820f, 0.5460f, 1.60f)
                lineTo(0f, 14.634f)
                lineToRelative(4.274f, -1.221f)
                curveToRelative(0.5180f, 0.2470f, 1.0510f, 0.4280f, 1.60f, 0.5460f)
                curveToRelative(0.5490f, 0.1170f, 1.1140f, 0.1760f, 1.6930f, 0.1760f)
                curveToRelative(0.9740f, 00f, 1.890f, -0.1820f, 2.7470f, -0.5460f)
                curveToRelative(0.8570f, -0.3640f, 1.6070f, -0.870f, 2.2480f, -1.5170f)
                curveToRelative(0.6410f, -0.6470f, 1.1470f, -1.3960f, 1.5170f, -2.2470f)
                curveToRelative(0.370f, -0.8510f, 0.5550f, -1.770f, 0.5550f, -2.7570f)
                curveToRelative(00f, -0.9740f, -0.1850f, -1.890f, -0.5550f, -2.7470f)
                curveToRelative(-0.370f, -0.8570f, -0.8760f, -1.6070f, -1.5170f, -2.2480f)
                curveToRelative(-0.6410f, -0.6410f, -1.3910f, -1.1470f, -2.2480f, -1.5170f)
                curveToRelative(-0.8570f, -0.370f, -1.7720f, -0.5550f, -2.7470f, -0.5550f)
                close()
                moveTo(7.57f, 4.032f)
                lineToRelative(0.895f, 2.117f)
                lineToRelative(2.3f, 0.2f)
                lineToRelative(-1.742f, 1.51f)
                lineToRelative(0.52f, 2.244f)
                lineToRelative(-1.974f, -1.19f)
                lineToRelative(-1.973f, 1.19f)
                lineToRelative(0.52f, -2.244f)
                lineToRelative(-1.742f, -1.51f)
                lineToRelative(2.3f, -0.2f)
                close()
            }
        }.build()
}
