package fr.outadoc.justchatting.utils.presentation

internal actual fun areBubblesSupported(): Boolean {
    // Bubbles are not supported on iOS
    return false
}
