package fr.outadoc.justchatting.component.twitch.server

import fr.outadoc.justchatting.component.twitch.api.HelixApi
import fr.outadoc.justchatting.component.twitch.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.EmoteSetResponse
import fr.outadoc.justchatting.component.twitch.model.FollowResponse
import fr.outadoc.justchatting.component.twitch.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.model.UsersResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path

class HelixServer(httpClient: HttpClient) : HelixApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://api.twitch.tv/helix/")
        }
    }

    override suspend fun getStreams(ids: List<String>): StreamsResponse {
        return client.get {
            url {
                path("streams")
                parameters.appendAll("user_id", ids)
            }
        }.body()
    }

    override suspend fun getFollowedStreams(
        userId: String?,
        limit: Int,
        offset: String?,
    ): StreamsResponse {
        return client.get {
            url {
                path("streams/followed")
                parameter("user_id", userId)
                parameter("first", limit)
                parameter("after", offset)
            }
        }.body()
    }

    override suspend fun getUsersById(ids: List<String>): UsersResponse {
        return client.get {
            url {
                path("users")
                parameters.appendAll("id", ids)
            }
        }.body()
    }

    override suspend fun getUsersByLogin(logins: List<String>): UsersResponse {
        return client.get {
            url {
                path("users")
                parameters.appendAll("login", logins)
            }
        }.body()
    }

    override suspend fun getChannels(
        query: String,
        limit: Int,
        offset: String?,
    ): ChannelSearchResponse {
        return client.get {
            url {
                path("search/channels")
                parameter("query", query)
                parameter("first", limit)
                parameter("after", offset)
            }
        }.body()
    }

    override suspend fun getFollowedChannels(
        userId: String?,
        limit: Int,
        offset: String?,
    ): FollowResponse {
        return client.get {
            url {
                path("users/follows")
                parameter("from_id", userId)
                parameter("first", limit)
                parameter("after", offset)
            }
        }.body()
    }

    override suspend fun getEmotesFromSet(setIds: List<String>): EmoteSetResponse {
        return client.get {
            url {
                path("chat/emotes/set")
                parameters.appendAll("emote_set_id", setIds)
            }
        }.body()
    }

    override suspend fun getCheerEmotes(userId: String?): CheerEmotesResponse {
        return client.get {
            url {
                path("bits/cheermotes")
                parameter("broadcaster_id", userId)
            }
        }.body()
    }
}