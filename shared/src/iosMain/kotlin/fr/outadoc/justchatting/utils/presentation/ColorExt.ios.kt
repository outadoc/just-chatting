package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
@OptIn(ExperimentalStdlibApi::class)
internal actual fun String.parseHexColor(): Color? {
    return Color(
        this.removePrefix("#")
            .lowercase()
            .hexToLong(),
    ).copy(alpha = 1f)
}
