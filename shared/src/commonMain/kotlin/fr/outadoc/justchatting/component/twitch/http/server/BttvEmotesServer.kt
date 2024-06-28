package fr.outadoc.justchatting.component.twitch.http.server

import fr.outadoc.justchatting.component.twitch.http.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.http.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.http.model.BttvEmote
import fr.outadoc.justchatting.component.twitch.http.model.FfzEmote
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

internal class BttvEmotesServer(httpClient: HttpClient) : BttvEmotesApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://api.betterttv.net/")
        }
    }

    override suspend fun getGlobalBttvEmotes(): Result<List<BttvEmote>> =
        runCatching {
            client.get { url { path("3/cached/emotes/global") } }.body()
        }

    override suspend fun getBttvEmotes(channelId: String): Result<BttvChannelResponse> =
        runCatching {
            client.get { url { path("3/cached/users/twitch", channelId) } }.body()
        }

    override suspend fun getBttvGlobalFfzEmotes(): Result<List<FfzEmote>> =
        runCatching {
            client.get { url { path("3/cached/frankerfacez/emotes/global") } }.body()
        }

    override suspend fun getBttvFfzEmotes(channelId: String): Result<List<FfzEmote>> =
        runCatching {
            client.get { url { path("3/cached/frankerfacez/users/twitch", channelId) } }.body()
        }
}
