package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/** Compose version of the shared [ensureColorIsAccessible]. */
@Stable
internal fun ensureColorIsAccessible(
    foreground: Color,
    background: Color,
    minimumContrast: Double = 4.5,
): Color? =
    ensureColorIsAccessible(
        foreground = foreground.toArgb(),
        background = background.toArgb(),
        minimumContrast = minimumContrast,
    )?.let { argb -> Color(argb) }
