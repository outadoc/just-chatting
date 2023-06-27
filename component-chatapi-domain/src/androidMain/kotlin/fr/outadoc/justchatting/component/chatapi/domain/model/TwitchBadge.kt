package fr.outadoc.justchatting.component.chatapi.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls

@Immutable
data class TwitchBadge(
    val setId: String,
    val title: String? = null,
    val version: String,
    val urls: EmoteUrls,
)
