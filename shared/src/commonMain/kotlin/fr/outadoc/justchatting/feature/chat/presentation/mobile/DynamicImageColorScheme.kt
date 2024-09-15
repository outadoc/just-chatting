package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
@Composable
internal expect fun dynamicImageColorScheme(
    url: String?,
    parentScheme: ColorScheme = MaterialTheme.colorScheme,
): ColorScheme

@Stable
@Composable
internal expect fun singleSourceColorScheme(
    color: Color?,
    parentScheme: ColorScheme = MaterialTheme.colorScheme,
): ColorScheme
