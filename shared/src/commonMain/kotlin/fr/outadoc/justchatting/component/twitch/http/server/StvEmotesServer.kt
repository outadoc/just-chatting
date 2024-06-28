package fr.outadoc.justchatting.component.twitch.http.server

import fr.outadoc.justchatting.component.twitch.http.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.http.model.StvEmoteResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

internal class StvEmotesServer(httpClient: HttpClient) : StvEmotesApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://7tv.io/")
        }
    }

    override suspend fun getGlobalStvEmotes(): Result<StvEmoteResponse> =
        runCatching {
            client.get { url { path("v3/emote-sets/62cdd34e72a832540de95857") } }.body()
        }
}
