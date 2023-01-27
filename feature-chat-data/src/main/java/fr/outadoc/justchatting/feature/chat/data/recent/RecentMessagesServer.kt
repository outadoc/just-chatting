package fr.outadoc.justchatting.feature.chat.data.recent

import fr.outadoc.justchatting.feature.chat.data.model.RecentMessagesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path

class RecentMessagesServer(httpClient: HttpClient) : RecentMessagesApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://recent-messages.robotty.de/api/")
        }
    }

    override suspend fun getRecentMessages(
        channelLogin: String,
        limit: Int
    ): RecentMessagesResponse {
        return client.get {
            url {
                path("v2/recent-messages", channelLogin)
                parameter("limit", limit)
            }
        }.body()
    }
}