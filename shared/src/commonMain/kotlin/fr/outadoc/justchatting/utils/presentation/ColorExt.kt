package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import fr.outadoc.justchatting.utils.logging.logError

@OptIn(ExperimentalStdlibApi::class)
@Stable
internal fun String.parseHexColor(): Color? {
    return try {
        val color: Long = removePrefix("#").lowercase().hexToLong()
        Color(color).copy(alpha = 1f)
    } catch (e: Exception) {
        logError("ColorExt", e) { "Failed to parse color from string: $this" }
        null
    }
}
