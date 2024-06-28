package fr.outadoc.justchatting.component.twitch.http.server

import fr.outadoc.justchatting.component.twitch.http.api.TwitchApi
import fr.outadoc.justchatting.component.twitch.http.model.ChannelScheduleResponse
import fr.outadoc.justchatting.component.twitch.http.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.http.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.http.model.EmoteSetResponse
import fr.outadoc.justchatting.component.twitch.http.model.FollowResponse
import fr.outadoc.justchatting.component.twitch.http.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.http.model.TwitchBadgesResponse
import fr.outadoc.justchatting.component.twitch.http.model.UsersResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path

internal class TwitchServer(httpClient: HttpClient) : TwitchApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://api.twitch.tv/helix/")
        }
    }

    override suspend fun getStreams(ids: List<String>): Result<StreamsResponse> =
        runCatching {
            client.get {
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
        after: String?,
    ): Result<StreamsResponse> =
        runCatching {
            client.get {
                url {
                    path("streams/followed")
                    parameter("user_id", userId)
                    parameter("first", limit)
                    after?.let { parameter("after", after) }
                }
            }.body()
        }

    override suspend fun getUsersById(ids: List<String>): Result<UsersResponse> =
        runCatching {
            client.get {
                url {
                    path("users")
                    ids.forEach { id ->
                        parameter("id", id)
                    }
                }
            }.body()
        }

    override suspend fun getUsersByLogin(logins: List<String>): Result<UsersResponse> =
        runCatching {
            client.get {
                url {
                    path("users")
                    logins.forEach { login ->
                        parameter("login", login)
                    }
                }
            }.body()
        }

    override suspend fun searchChannels(
        query: String,
        limit: Int,
        after: String?,
    ): Result<ChannelSearchResponse> =
        runCatching {
            client.get {
                url {
                    path("search/channels")
                    parameter("query", query)
                    parameter("first", limit)
                    after?.let { parameter("after", after) }
                }
            }.body()
        }

    override suspend fun getFollowedChannels(
        userId: String?,
        limit: Int,
        after: String?,
    ): Result<FollowResponse> =
        runCatching {
            client.get {
                url {
                    path("channels/followed")
                    parameter("user_id", userId)
                    parameter("first", limit)
                    after?.let { parameter("after", after) }
                }
            }.body()
        }

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<EmoteSetResponse> =
        runCatching {
            client.get {
                url {
                    path("chat/emotes/set")
                    setIds.forEach { id ->
                        parameter("emote_set_id", id)
                    }
                }
            }.body()
        }

    override suspend fun getCheerEmotes(userId: String?): Result<CheerEmotesResponse> =
        runCatching {
            client.get {
                url {
                    path("bits/cheermotes")
                    parameter("broadcaster_id", userId)
                }
            }.body()
        }

    override suspend fun getGlobalBadges(): Result<TwitchBadgesResponse> =
        runCatching {
            client.get { url { path("chat/badges/global") } }.body()
        }

    override suspend fun getChannelBadges(channelId: String): Result<TwitchBadgesResponse> =
        runCatching {
            client.get {
                url {
                    path("chat/badges")
                    parameter("broadcaster_id", channelId)
                }
            }.body()
        }

    override suspend fun getChannelSchedule(
        channelId: String,
        limit: Int,
        after: String?,
    ): Result<ChannelScheduleResponse> =
        runCatching {
            val response = client.get {
                url {
                    path("schedule")
                    parameter("broadcaster_id", channelId)
                    parameter("first", limit)
                    after?.let { parameter("after", after) }
                }
            }

            if (response.status.value == 404) {
                // Channel has no schedule
                error("Channel has no schedule")
            } else {
                response.body()
            }
        }
}
