package fr.outadoc.justchatting.component.chatapi.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class TwitchBadge(
    val id: String,
    val version: String,
    private val urls: ImmutableMap<Float, String>,
    val title: String? = null
) {
    @Stable
    fun getUrl(screenDensity: Float): String {
        return urls
            .toList()
            .minByOrNull { url -> screenDensity - url.first }
            ?.second
            ?: error("No URLs were provided for this TwitchBadge")
    }
}
