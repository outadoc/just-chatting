package fr.outadoc.justchatting.utils.presentation

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance

internal val ColorScheme.isDark: Boolean
    get() = background.luminance() < 0.5
