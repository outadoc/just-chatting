package fr.outadoc.justchatting.utils.presentation

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal expect fun getAppColorScheme(isDarkTheme: Boolean): ColorScheme
