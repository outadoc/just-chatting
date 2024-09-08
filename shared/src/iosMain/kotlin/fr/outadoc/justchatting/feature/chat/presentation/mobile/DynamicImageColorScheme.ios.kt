package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
@Composable
internal actual fun dynamicImageColorScheme(
    url: String?,
    parentScheme: ColorScheme,
): ColorScheme {
    return parentScheme
}

internal actual fun darkSchemeFromColor(color: Color): ColorScheme {
    TODO("not implemented")
}

internal actual fun lightSchemeFromColor(color: Color): ColorScheme {
    TODO("not implemented")
}
