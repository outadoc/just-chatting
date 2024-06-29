package fr.outadoc.justchatting.component.chatapi.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls

@Immutable
internal data class TwitchBadge(
    val setId: String,
    val title: String? = null,
    val version: String,
    val urls: EmoteUrls,
)
