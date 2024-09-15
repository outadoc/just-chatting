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
    // TODO implement for iOS
    return parentScheme
}

@Composable
@Stable
internal actual fun singleSourceColorScheme(
    color: Color?,
    parentScheme: ColorScheme,
): ColorScheme {
    // TODO implement for iOS
    return parentScheme
}
