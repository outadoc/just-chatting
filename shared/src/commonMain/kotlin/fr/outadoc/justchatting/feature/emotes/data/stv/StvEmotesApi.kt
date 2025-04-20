package fr.outadoc.justchatting.feature.emotes.data.stv

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote

internal interface StvEmotesApi {
    suspend fun getGlobalStvEmotes(): Result<List<Emote>>
    suspend fun getStvEmotes(channelId: String): Result<List<Emote>>
}
