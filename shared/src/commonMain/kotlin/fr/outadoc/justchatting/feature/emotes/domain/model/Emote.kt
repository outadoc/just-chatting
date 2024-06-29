package fr.outadoc.justchatting.feature.emotes.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class Emote(
    val name: String,
    val urls: EmoteUrls,
    val ownerId: String? = null,
    val isZeroWidth: Boolean = false,
    val bitsValue: Int? = null,
    val colorHex: String? = null,
)
