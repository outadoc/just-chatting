package fr.outadoc.justchatting.utils.presentation

import fr.outadoc.justchatting.utils.logging.logError
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

// Colors are handled as ARGB ints here, so that every platform can use them.

private const val MIN_LUMINANCE_SEARCH_MAX_ITERATIONS = 10
private const val MIN_LUMINANCE_SEARCH_PRECISION = 1

/** Parses a `#RRGGBB` color into an opaque ARGB color. */
@OptIn(ExperimentalStdlibApi::class)
public fun String.parseHexColorArgb(): Int? =
    try {
        (removePrefix("#").lowercase().hexToLong() or 0xFF000000).toInt()
    } catch (e: Exception) {
        logError("ColorContrast", e) { "Failed to parse color from string: $this" }
        null
    }

/**
 * Returns [foreground], made lighter if needed so that it has at least [minimumContrast] with
 * [background], or null if even the lightest version of it wouldn't be enough.
 */
public fun ensureColorIsAccessible(
    foreground: Int,
    background: Int,
    minimumContrast: Double = 4.5,
): Int? {
    // Get X, Y, Z components of foreground and background colors
    val (_, backgroundY, _) = argbToXyz(background)
    val (foregroundX, foregroundY, foregroundZ) = argbToXyz(foreground)

    // Calculate the contrast between the foreground and background colors
    val contrast =
        calculateLuminanceContrast(
            foregroundY = foregroundY,
            backgroundY = backgroundY,
        )

    // Contrast is a-ok as-is
    if (contrast > minimumContrast) return foreground

    val maxLuminanceContrast =
        calculateLuminanceContrast(
            foregroundY = 100.0,
            backgroundY = backgroundY,
        )

    // Even with max luminance, contrast isn't high enough
    if (maxLuminanceContrast < minimumContrast) return null

    // Binary search to find a value with the minimum value which provides sufficient contrast
    var numIterations = 0

    var minNewY = 0.0
    var maxNewY = 100.0

    while (
        numIterations <= MIN_LUMINANCE_SEARCH_MAX_ITERATIONS &&
        maxNewY - minNewY > MIN_LUMINANCE_SEARCH_PRECISION
    ) {
        val testY = (minNewY + maxNewY) / 2
        val testContrast =
            calculateLuminanceContrast(
                foregroundY = testY,
                backgroundY = backgroundY,
            )

        if (testContrast < minimumContrast) {
            minNewY = testY
        } else {
            maxNewY = testY
        }

        numIterations++
    }

    // Return the foreground color, but with a higher luminance contrast
    return xyzToArgb(
        x = foregroundX,
        y = maxNewY,
        z = foregroundZ,
    )
}

private fun calculateLuminanceContrast(
    foregroundY: Double,
    backgroundY: Double,
): Double = max(foregroundY + 5, backgroundY + 5) / min(foregroundY + 5, backgroundY + 5)

/**
 * Converts a color from CIE XYZ to an opaque ARGB color.
 *
 * This method expects the XYZ representation to use the D65 illuminant and the CIE
 * 2° Standard Observer (1931).
 *
 * @param x X component value [0, 95.047)
 * @param y Y component value [0, 100)
 * @param z Z component value [0, 108.883)
 */
private fun xyzToArgb(
    x: Double,
    y: Double,
    z: Double,
): Int {
    var r = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100
    var g = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100
    var b = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100

    r = if (r > 0.0031308) 1.055 * r.pow(1 / 2.4) - 0.055 else 12.92 * r
    g = if (g > 0.0031308) 1.055 * g.pow(1 / 2.4) - 0.055 else 12.92 * g
    b = if (b > 0.0031308) 1.055 * b.pow(1 / 2.4) - 0.055 else 12.92 * b

    fun Double.toComponent(): Int = round(this * 255).toInt().coerceIn(0, 255)

    return (0xFF shl 24) or (r.toComponent() shl 16) or (g.toComponent() shl 8) or b.toComponent()
}

/**
 * Converts an ARGB color to its CIE XYZ components.
 *
 * The resulting XYZ representation will use the D65 illuminant and the CIE
 * 2° Standard Observer (1931).
 *
 *  * outXyz[0] is X [0, 95.047)
 *  * outXyz[1] is Y [0, 100)
 *  * outXyz[2] is Z [0, 108.883)
 */
private fun argbToXyz(argb: Int): DoubleArray {
    fun Int.toLinear(): Double {
        val c = this / 255.0
        return if (c < 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    val sr = ((argb shr 16) and 0xFF).toLinear()
    val sg = ((argb shr 8) and 0xFF).toLinear()
    val sb = (argb and 0xFF).toLinear()

    return doubleArrayOf(
        100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805),
        100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722),
        100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505),
    )
}
