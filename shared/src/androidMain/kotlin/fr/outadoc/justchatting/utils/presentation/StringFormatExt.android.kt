package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.desc.PluralFormattedStringDesc
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

@Composable
internal actual fun PluralFormattedStringDesc.asString(): String {
    return toString(LocalContext.current)
}

@Composable
internal actual fun ResourceFormattedStringDesc.asString(): String {
    return toString(LocalContext.current)
}
