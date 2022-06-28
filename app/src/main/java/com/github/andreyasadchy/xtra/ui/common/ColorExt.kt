package com.github.andreyasadchy.xtra.ui.common

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.math.max
import kotlin.math.min

@ColorInt
fun ensureColorIsAccessible(
    @ColorInt background: Int,
    @ColorInt foreground: Int,
    minimumContrast: Double = 4.5
): Int {
    // Get X, Y, Z components of foreground and background colors
    val (_, backgroundY, _) = colorToXyz(background)
    val (foregroundX, foregroundY, foregroundZ) = colorToXyz(foreground)

    // Calculate the contrast between the foreground and background colors
    val contrast = max(foregroundY + 5, backgroundY + 5) / min(foregroundY + 5, backgroundY + 5)

    if (contrast > minimumContrast) return foreground

    // Return the foreground color, but with a higher luminance contrast
    val correctedForeground = xyzToColor(
        x = foregroundX,
        y = if (backgroundY > foregroundY) {
            (backgroundY + 5) / minimumContrast
        } else {
            (backgroundY + 5) * minimumContrast
        }.coerceAtMost(100.0),
        z = foregroundZ
    )

    return correctedForeground
}

@ColorInt
fun ensureMinimumAlpha(
    @ColorInt background: Int,
    @ColorInt foreground: Int,
    minimumContrast: Float = 10.0f
): Int {
    val minAlpha = ColorUtils.calculateMinimumAlpha(foreground, background, minimumContrast)
        .takeIf { it >= 0 } ?: 255

    return ColorUtils.setAlphaComponent(
        foreground,
        minAlpha
    )
}

private fun colorToXyz(@ColorInt color: Int): DoubleArray {
    return DoubleArray(3).apply {
        ColorUtils.colorToXYZ(color, this)
    }
}

@ColorInt
private fun xyzToColor(x: Double, y: Double, z: Double): Int = ColorUtils.XYZToColor(x, y, z)

fun Int.colorToHex(): String = "#%06X".format(0xFFFFFF and this)
