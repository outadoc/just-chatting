package fr.outadoc.justchatting.utils.presentation

import androidx.compose.ui.graphics.Color

internal expect fun String.parseHexColor(): Color?

internal expect fun calculateLuminanceContrast(
    foregroundY: Double,
    backgroundY: Double,
): Double

internal expect fun colorToXyz(color: Color): DoubleArray

internal expect fun xyzToColor(
    x: Double,
    y: Double,
    z: Double,
): Color
