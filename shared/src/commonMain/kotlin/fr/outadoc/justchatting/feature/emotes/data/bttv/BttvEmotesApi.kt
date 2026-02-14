package fr.outadoc.justchatting.feature.emotes.data.bttv

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote

internal interface BttvEmotesApi {
    suspend fun getGlobalBttvEmotes(): Result<List<Emote>>

    suspend fun getBttvEmotes(channelId: String): Result<List<Emote>>

    suspend fun getBttvGlobalFfzEmotes(): Result<List<Emote>>

    suspend fun getBttvFfzEmotes(channelId: String): Result<List<Emote>>
}
