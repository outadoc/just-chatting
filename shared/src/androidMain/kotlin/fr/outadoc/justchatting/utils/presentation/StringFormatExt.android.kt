package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.desc.PluralFormattedStringDesc
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

@Stable
@Composable
internal actual fun PluralFormattedStringDesc.asString(): String {
    return toString(LocalContext.current)
}

@Stable
@Composable
internal actual fun ResourceFormattedStringDesc.asString(): String {
    return toString(LocalContext.current)
}
