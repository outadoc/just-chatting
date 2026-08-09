package fr.outadoc.justchatting.utils.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
internal actual fun isAppInDarkTheme(): Boolean = isSystemInDarkTheme()
