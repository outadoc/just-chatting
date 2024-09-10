package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable

internal expect fun areBubblesSupported(): Boolean

@Composable
internal expect fun canOpenActivityInBubble(): Boolean
