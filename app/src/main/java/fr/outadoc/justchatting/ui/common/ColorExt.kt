package fr.outadoc.justchatting.ui.common

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.max
import kotlin.math.min

private const val MIN_LUMINANCE_SEARCH_MAX_ITERATIONS = 10
private const val MIN_LUMINANCE_SEARCH_PRECISION = 1

fun ensureColorIsAccessible(
    foreground: Color,
    background: Color,
    minimumContrast: Double = 4.5
): Color? {
    // Get X, Y, Z components of foreground and background colors
    val (_, backgroundY, _) = colorToXyz(background)
    val (foregroundX, foregroundY, foregroundZ) = colorToXyz(foreground)

    // Calculate the contrast between the foreground and background colors
    val contrast = calculateLuminanceContrast(
        foregroundY = foregroundY,
        backgroundY = backgroundY
    )

    // Contrast is a-ok as-is
    if (contrast > minimumContrast) return foreground

    val maxLuminanceContrast = calculateLuminanceContrast(
        foregroundY = 100.0,
        backgroundY = backgroundY
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
        val testContrast = calculateLuminanceContrast(
            foregroundY = testY,
            backgroundY = backgroundY
        )

        if (testContrast < minimumContrast) minNewY = testY
        else maxNewY = testY

        numIterations++
    }

    // Return the foreground color, but with a higher luminance contrast
    return xyzToColor(
        x = foregroundX,
        y = maxNewY,
        z = foregroundZ
    )
}

private fun calculateLuminanceContrast(foregroundY: Double, backgroundY: Double): Double {
    return max(foregroundY + 5, backgroundY + 5) / min(foregroundY + 5, backgroundY + 5)
}

@ColorInt
fun ensureMinimumAlpha(
    @ColorInt foreground: Int,
    @ColorInt background: Int,
    minimumContrast: Float = 10.0f
): Int {
    val minAlpha = ColorUtils.calculateMinimumAlpha(foreground, background, minimumContrast)
        .takeIf { it >= 0 } ?: 255

    return ColorUtils.setAlphaComponent(
        foreground,
        minAlpha
    )
}

private fun colorToXyz(color: Color): DoubleArray {
    return DoubleArray(3).apply {
        ColorUtils.colorToXYZ(color.toArgb(), this)
    }
}

private fun xyzToColor(x: Double, y: Double, z: Double) = Color(ColorUtils.XYZToColor(x, y, z))

val Color.isLight: Boolean
    get() = luminance() > 0.5

fun String.parseHexColor(): Color =
    Color(android.graphics.Color.parseColor(this))
