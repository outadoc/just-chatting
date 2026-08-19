package fr.outadoc.justchatting.feature.emotes.data.ffz

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote

internal interface FfzEmotesApi {
    suspend fun getGlobalFfzEmotes(): Result<List<Emote>>

    suspend fun getFfzEmotes(channelId: String): Result<List<Emote>>
}
