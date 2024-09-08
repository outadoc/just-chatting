package fr.outadoc.justchatting.utils.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.max
import kotlin.math.min

internal actual fun calculateLuminanceContrast(foregroundY: Double, backgroundY: Double): Double {
    return max(foregroundY + 5, backgroundY + 5) / min(foregroundY + 5, backgroundY + 5)
}

internal actual fun colorToXyz(color: Color): DoubleArray {
    return DoubleArray(3).apply {
        ColorUtils.colorToXYZ(color.toArgb(), this)
    }
}

internal actual fun xyzToColor(x: Double, y: Double, z: Double): Color {
    return Color(ColorUtils.XYZToColor(x, y, z))
}

internal actual fun String.parseHexColor(): Color? =
    try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: IllegalArgumentException) {
        null
    }
