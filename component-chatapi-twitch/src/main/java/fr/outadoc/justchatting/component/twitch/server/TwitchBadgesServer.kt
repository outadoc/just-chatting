package fr.outadoc.justchatting.component.twitch.server

import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

class TwitchBadgesServer(httpClient: HttpClient) : TwitchBadgesApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://badges.twitch.tv/")
        }
    }

    override suspend fun getGlobalBadges(): TwitchBadgesResponse =
        client.get { url { path("v1/badges/global/display") } }.body()

    override suspend fun getChannelBadges(channelId: String): TwitchBadgesResponse =
        client.get { url { path("v1/badges/channels", channelId, "display") } }.body()
}