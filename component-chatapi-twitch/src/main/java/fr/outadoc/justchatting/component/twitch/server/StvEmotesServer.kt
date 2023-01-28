package fr.outadoc.justchatting.component.twitch.server

import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.model.StvEmote
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

class StvEmotesServer(httpClient: HttpClient) : StvEmotesApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://api.7tv.app/")
        }
    }

    override suspend fun getGlobalStvEmotes(): List<StvEmote> =
        client.get { url { path("v2/emotes/global") } }.body()

    override suspend fun getStvEmotes(channelId: String): List<StvEmote> =
        client.get { url { path("v2/users", channelId, "emotes") } }.body()
}
