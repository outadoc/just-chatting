package fr.outadoc.justchatting.feature.emotes.data.bttv

import fr.outadoc.justchatting.feature.emotes.data.bttv.model.BttvChannelResponse
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.BttvEmote
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.FfzEmote

internal interface BttvEmotesApi {

    suspend fun getGlobalBttvEmotes(): Result<List<BttvEmote>>

    suspend fun getBttvEmotes(channelId: String): Result<BttvChannelResponse>

    suspend fun getBttvGlobalFfzEmotes(): Result<List<FfzEmote>>

    suspend fun getBttvFfzEmotes(channelId: String): Result<List<FfzEmote>>
}
