package fr.outadoc.justchatting.feature.chat.data.irc.recent

import fr.outadoc.justchatting.feature.shared.data.ApiEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path

internal class RecentMessagesServer(httpClient: HttpClient) : RecentMessagesApi {

    private val client = httpClient.config {
        defaultRequest {
            url(ApiEndpoints.RECENT_MESSAGES)
        }
    }

    override suspend fun getRecentMessages(
        channelLogin: String,
        limit: Int,
    ): Result<RecentMessagesResponse> {
        return runCatching {
            client.get {
                url {
                    path("v2/recent-messages", channelLogin)
                    parameter("limit", limit)
                }
            }.body()
        }
    }
}
