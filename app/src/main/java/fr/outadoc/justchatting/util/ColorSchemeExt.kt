package fr.outadoc.justchatting.util

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

@get:Composable
val ColorScheme.isLight: Boolean
    get() = this.background.luminance() > 0.5
