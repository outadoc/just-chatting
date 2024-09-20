package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
internal expect fun String.parseHexColor(): Color?
