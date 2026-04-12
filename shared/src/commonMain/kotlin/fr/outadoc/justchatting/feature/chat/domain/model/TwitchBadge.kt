package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls

@Immutable
public data class TwitchBadge(
    val setId: String,
    val title: String? = null,
    val version: String,
    val urls: EmoteUrls,
)
