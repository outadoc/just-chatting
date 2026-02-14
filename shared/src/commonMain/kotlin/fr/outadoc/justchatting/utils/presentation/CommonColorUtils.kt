package fr.outadoc.justchatting.utils.presentation

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

internal fun calculateLuminanceContrast(
    foregroundY: Double,
    backgroundY: Double,
): Double {
    return max(foregroundY + 5, backgroundY + 5) / min(foregroundY + 5, backgroundY + 5)
}

/**
 * Converts a color from CIE XYZ to its RGB representation.
 *
 * This method expects the XYZ representation to use the D65 illuminant and the CIE
 * 2° Standard Observer (1931).
 *
 * @param x X component value [0, 95.047)
 * @param y Y component value [0, 100)
 * @param z Z component value [0, 108.883)
 * @return int containing the RGB representation
 */
@ColorInt
internal fun xyzToColor(
    @FloatRange(from = .0, to = XYZ_WHITE_REFERENCE_X) x: Double,
    @FloatRange(from = .0, to = XYZ_WHITE_REFERENCE_Y) y: Double,
    @FloatRange(from = .0, to = XYZ_WHITE_REFERENCE_Z) z: Double,
): Color {
    var r = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100
    var g = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100
    var b = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100

    r = if (r > 0.0031308) 1.055 * r.pow(1 / 2.4) - 0.055 else 12.92 * r
    g = if (g > 0.0031308) 1.055 * g.pow(1 / 2.4) - 0.055 else 12.92 * g
    b = if (b > 0.0031308) 1.055 * b.pow(1 / 2.4) - 0.055 else 12.92 * b

    return Color(
        red = round(r * 255).toInt().coerceIn(0, 255),
        green = round(g * 255).toInt().coerceIn(0, 255),
        blue = round(b * 255).toInt().coerceIn(0, 255),
    )
}

/**
 * Convert RGB components to its CIE XYZ representative components.
 *
 * The resulting XYZ representation will use the D65 illuminant and the CIE
 * 2° Standard Observer (1931).
 *
 *  * outXyz[0] is X [0, 95.047)
 *  * outXyz[1] is Y [0, 100)
 *  * outXyz[2] is Z [0, 108.883)
 *
 * @param color The color to be converted
 */
internal fun colorToXyz(color: Color): DoubleArray {
    val r: Double = color.red.toDouble()
    val sr = if (r < 0.04045) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)

    val g: Double = color.green.toDouble()
    val sg = if (g < 0.04045) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)

    val b: Double = color.blue.toDouble()
    val sb = if (b < 0.04045) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)

    return doubleArrayOf(
        100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805),
        100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722),
        100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505),
    )
}

private const val XYZ_WHITE_REFERENCE_X = 95.047
private const val XYZ_WHITE_REFERENCE_Y = 100.0
private const val XYZ_WHITE_REFERENCE_Z = 108.883
