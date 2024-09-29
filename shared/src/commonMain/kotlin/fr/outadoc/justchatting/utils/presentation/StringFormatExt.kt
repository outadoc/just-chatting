package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import dev.icerock.moko.resources.desc.PluralFormattedStringDesc
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

@Stable
@Composable
internal expect fun PluralFormattedStringDesc.asString(): String

@Stable
@Composable
internal expect fun ResourceFormattedStringDesc.asString(): String
