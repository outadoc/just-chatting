package fr.outadoc.justchatting.utils.presentation

import androidx.compose.ui.graphics.Color

internal actual fun String.parseHexColor(): Color? =
    try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: IllegalArgumentException) {
        null
    }
