package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.http.model.BttvEmote
import fr.outadoc.justchatting.component.twitch.http.model.FfzEmote

interface BttvEmotesApi {

    suspend fun getGlobalBttvEmotes(): List<BttvEmote>

    suspend fun getBttvEmotes(channelId: String): BttvChannelResponse

    suspend fun getBttvGlobalFfzEmotes(): List<FfzEmote>

    suspend fun getBttvFfzEmotes(channelId: String): List<FfzEmote>
}
