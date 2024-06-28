package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.http.model.BttvEmote
import fr.outadoc.justchatting.component.twitch.http.model.FfzEmote

internal interface BttvEmotesApi {

    suspend fun getGlobalBttvEmotes(): Result<List<BttvEmote>>

    suspend fun getBttvEmotes(channelId: String): Result<BttvChannelResponse>

    suspend fun getBttvGlobalFfzEmotes(): Result<List<FfzEmote>>

    suspend fun getBttvFfzEmotes(channelId: String): Result<List<FfzEmote>>
}
