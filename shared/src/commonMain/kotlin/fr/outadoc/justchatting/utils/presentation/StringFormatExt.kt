package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.desc.PluralFormattedStringDesc
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

@Composable
internal expect fun PluralFormattedStringDesc.asString(): String

@Composable
internal expect fun ResourceFormattedStringDesc.asString(): String
