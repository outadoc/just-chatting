package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable

@Composable
internal expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
