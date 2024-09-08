package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable

@Composable
internal actual fun canOpenInBubble(): Boolean {
    // Can't open in bubble on iOS
    return false
}
