package fr.outadoc.justchatting.feature.emotes.data.ffz

import fr.outadoc.justchatting.feature.emotes.data.ffz.model.FfzSetsResponse
import fr.outadoc.justchatting.feature.emotes.data.ffz.model.map
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.shared.data.ApiEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

internal class FfzEmotesServer(
    httpClient: HttpClient,
) : FfzEmotesApi {
    private val client =
        httpClient.config {
            defaultRequest {
                url(ApiEndpoints.FFZ_BASE)
            }
        }

    override suspend fun getGlobalFfzEmotes(): Result<List<Emote>> =
        runCatching {
            client
                .get { url { path("set/global") } }
                .body<FfzSetsResponse>()
        }.map { response -> response.toEmotes() }

    override suspend fun getFfzEmotes(channelId: String): Result<List<Emote>> =
        runCatching {
            client
                .get { url { path("room/id", channelId) } }
                .body<FfzSetsResponse>()
        }.map { response -> response.toEmotes() }

    private fun FfzSetsResponse.toEmotes(): List<Emote> =
        sets.values
            .flatMap { set -> set.emoticons }
            .map { emoticon -> emoticon.map() }
}
