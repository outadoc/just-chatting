package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
@Composable
internal actual fun dynamicImageColorScheme(
    url: String?,
    parentScheme: ColorScheme
): ColorScheme {
    return parentScheme
}
