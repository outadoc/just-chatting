package fr.outadoc.justchatting.feature.emotes.data.stv

import fr.outadoc.justchatting.feature.emotes.data.stv.model.StvEmoteResponse
import fr.outadoc.justchatting.feature.emotes.data.stv.model.map
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.shared.data.ApiEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

internal class StvEmotesServer(httpClient: HttpClient) : StvEmotesApi {

    private val client = httpClient.config {
        defaultRequest {
            url(ApiEndpoints.STV_BASE)
        }
    }

    override suspend fun getGlobalStvEmotes(): Result<List<Emote>> =
        runCatching {
            client
                .get { url { path("v3/emote-sets/62cdd34e72a832540de95857") } }
                .body<StvEmoteResponse>()
        }.map { response ->
            response.emotes
                .map { emote -> emote.map() }
        }
}
