package fr.outadoc.justchatting.feature.emotes.data.ffz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FfzSet(
    @SerialName("emoticons")
    val emoticons: List<FfzEmoticon>,
)
