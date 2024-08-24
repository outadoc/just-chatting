package fr.outadoc.justchatting.feature.emotes.data.bttv

import fr.outadoc.justchatting.feature.emotes.data.bttv.model.BttvChannelResponse
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.BttvEmote
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.FfzEmote
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.map
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.shared.data.ApiEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

internal class BttvEmotesServer(httpClient: HttpClient) : BttvEmotesApi {

    private val client = httpClient.config {
        defaultRequest {
            url(ApiEndpoints.BTTV_BASE)
        }
    }

    override suspend fun getGlobalBttvEmotes(): Result<List<Emote>> =
        runCatching {
            client
                .get { url { path("3/cached/emotes/global") } }
                .body<List<BttvEmote>>()
        }.map { response ->
            response.map { emote -> emote.map() }
        }

    override suspend fun getBttvEmotes(channelId: String): Result<List<Emote>> =
        runCatching {
            client
                .get { url { path("3/cached/users/twitch", channelId) } }
                .body<BttvChannelResponse>()
        }.map { response ->
            response
                .allEmotes
                .map { emote -> emote.map() }
        }

    override suspend fun getBttvGlobalFfzEmotes(): Result<List<Emote>> =
        runCatching {
            client
                .get { url { path("3/cached/frankerfacez/emotes/global") } }
                .body<List<FfzEmote>>()
        }.map { response ->
            response.map { emote -> emote.map() }
        }

    override suspend fun getBttvFfzEmotes(channelId: String): Result<List<Emote>> =
        runCatching {
            client
                .get { url { path("3/cached/frankerfacez/users/twitch", channelId) } }
                .body<List<FfzEmote>>()
        }.map { response ->
            response.map { emote -> emote.map() }
        }
}
