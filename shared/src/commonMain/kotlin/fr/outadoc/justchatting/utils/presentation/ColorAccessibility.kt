package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

private const val MIN_LUMINANCE_SEARCH_MAX_ITERATIONS = 10
private const val MIN_LUMINANCE_SEARCH_PRECISION = 1

@Stable
internal fun ensureColorIsAccessible(
    foreground: Color,
    background: Color,
    minimumContrast: Double = 4.5,
): Color? {
    // Get X, Y, Z components of foreground and background colors
    val (_, backgroundY, _) = colorToXyz(background)
    val (foregroundX, foregroundY, foregroundZ) = colorToXyz(foreground)

    // Calculate the contrast between the foreground and background colors
    val contrast = calculateLuminanceContrast(
        foregroundY = foregroundY,
        backgroundY = backgroundY,
    )

    // Contrast is a-ok as-is
    if (contrast > minimumContrast) return foreground

    val maxLuminanceContrast = calculateLuminanceContrast(
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
        val testContrast = calculateLuminanceContrast(
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
    return xyzToColor(
        x = foregroundX,
        y = maxNewY,
        z = foregroundZ,
    )
}
