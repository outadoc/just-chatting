package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import dev.icerock.moko.resources.desc.PluralFormattedStringDesc
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

@Stable
@Composable
internal actual fun PluralFormattedStringDesc.asString(): String {
    return localized()
}

@Stable
@Composable
internal actual fun ResourceFormattedStringDesc.asString(): String {
    return localized()
}
