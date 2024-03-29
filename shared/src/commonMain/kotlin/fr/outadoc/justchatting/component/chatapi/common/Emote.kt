package fr.outadoc.justchatting.component.chatapi.common

import androidx.compose.runtime.Immutable

@Immutable
data class Emote(
    val name: String,
    val urls: EmoteUrls,
    val ownerId: String? = null,
    val isZeroWidth: Boolean = false,
    val bitsValue: Int? = null,
    val colorHex: String? = null,
)
