package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode

@Composable
internal actual fun isAppInDarkTheme(): Boolean = isSystemInDarkMode()
