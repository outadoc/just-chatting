package fr.outadoc.justchatting.component.twitch.http.server

import fr.outadoc.justchatting.component.twitch.http.api.HelixApi
import fr.outadoc.justchatting.component.twitch.http.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.http.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.http.model.EmoteSetResponse
import fr.outadoc.justchatting.component.twitch.http.model.FollowResponse
import fr.outadoc.justchatting.component.twitch.http.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.http.model.Subscription
import fr.outadoc.justchatting.component.twitch.http.model.UsersResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
                ids.forEach { id ->
                    parameter("user_id", id)
                }
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
                ids.forEach { id ->
                    parameter("id", id)
                }
            }
        }.body()
    }

    override suspend fun getUsersByLogin(logins: List<String>): UsersResponse {
        return client.get {
            url {
                path("users")
                logins.forEach { login ->
                    parameter("login", login)
                }
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
                setIds.forEach { id ->
                    parameter("emote_set_id", id)
                }
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

    override suspend fun createSubscription(type: String, channelId: String, sessionId: String) {
        client.post {
            url {
                path("eventsub/subscriptions")
                contentType(ContentType.Application.Json)
                setBody(
                    Subscription(
                        type = type,
                        condition = Subscription.Condition(
                            broadcasterUserId = channelId,
                        ),
                        transport = Subscription.Transport(sessionId = sessionId),
                    ),
                )
            }
        }
    }
}
