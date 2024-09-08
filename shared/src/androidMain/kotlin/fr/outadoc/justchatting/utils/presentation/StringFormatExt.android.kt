package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.desc.PluralFormattedStringDesc

@Composable
internal actual fun PluralFormattedStringDesc.asString(): String {
    return toString(LocalContext.current)
}
