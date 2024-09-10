package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable

internal actual fun areBubblesSupported(): Boolean {
    // Bubbles are not supported on iOS
    return false
}

@Composable
internal actual fun canOpenActivityInBubble(): Boolean {
    return false
}
