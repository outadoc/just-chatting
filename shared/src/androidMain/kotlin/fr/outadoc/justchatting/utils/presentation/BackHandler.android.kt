package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable

@Composable
internal actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(
        enabled = enabled,
        onBack = onBack,
    )
}
