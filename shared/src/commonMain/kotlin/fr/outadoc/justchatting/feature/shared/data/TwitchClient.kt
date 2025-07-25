package fr.outadoc.justchatting.feature.shared.data

import fr.outadoc.justchatting.feature.chat.data.http.CheerEmotesResponse
import fr.outadoc.justchatting.feature.chat.data.http.SendMessageResponse
import fr.outadoc.justchatting.feature.chat.data.http.TwitchBadgesResponse
import fr.outadoc.justchatting.feature.emotes.data.twitch.model.EmoteSetResponse
import fr.outadoc.justchatting.feature.followed.data.model.FollowResponse
import fr.outadoc.justchatting.feature.search.data.model.ChannelSearchResponse
import fr.outadoc.justchatting.feature.shared.data.model.UsersResponse
import fr.outadoc.justchatting.feature.timeline.data.model.ChannelScheduleResponse
import fr.outadoc.justchatting.feature.timeline.data.model.StreamsResponse
import fr.outadoc.justchatting.feature.timeline.data.model.VideoResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.path
import kotlin.time.Instant

internal class TwitchClient(httpClient: HttpClient) {

    private val client = httpClient.config {
        defaultRequest {
            url(ApiEndpoints.TWITCH_HELIX)
        }
    }

    suspend fun getStreamsByUserId(ids: List<String>): Result<StreamsResponse> =
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

    suspend fun getStreamsByUserLogin(logins: List<String>): Result<StreamsResponse> =
        runCatching {
            client.get {
                url {
                    path("streams")
                    logins.forEach { id ->
                        parameter("user_login", id)
                    }
                }
            }.body()
        }

    suspend fun getFollowedStreams(
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

    suspend fun getUsersById(ids: List<String>): Result<UsersResponse> =
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

    suspend fun getUsersByLogin(logins: List<String>): Result<UsersResponse> =
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

    suspend fun searchChannels(
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

    suspend fun getFollowedChannels(
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

    suspend fun getEmotesFromSet(setIds: List<String>): Result<EmoteSetResponse> =
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

    suspend fun getCheerEmotes(userId: String?): Result<CheerEmotesResponse> =
        runCatching {
            client.get {
                url {
                    path("bits/cheermotes")
                    parameter("broadcaster_id", userId)
                }
            }.body()
        }

    suspend fun getGlobalBadges(): Result<TwitchBadgesResponse> =
        runCatching {
            client.get { url { path("chat/badges/global") } }.body()
        }

    suspend fun getChannelBadges(channelId: String): Result<TwitchBadgesResponse> =
        runCatching {
            client.get {
                url {
                    path("chat/badges")
                    parameter("broadcaster_id", channelId)
                }
            }.body()
        }

    suspend fun getChannelSchedule(
        userId: String,
        start: Instant,
        limit: Int,
        after: String?,
    ): Result<ChannelScheduleResponse> =
        runCatching {
            val response = client.get {
                url {
                    path("schedule")
                    parameter("broadcaster_id", userId)
                    parameter("first", limit)
                    parameter("start_time", start.toString())
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

    suspend fun getChannelVideos(
        userId: String,
        limit: Int,
        after: String?,
        before: String? = null,
    ): Result<VideoResponse> =
        runCatching {
            client.get {
                url {
                    path("videos")
                    parameter("user_id", userId)
                    parameter("first", limit)
                    parameter("type", "archive")
                    parameter("sort", "time")
                    parameter("period", "month")
                    after?.let { parameter("after", after) }
                    before?.let { parameter("before", before) }
                }
            }.body()
        }

    suspend fun sendChatMessage(
        channelUserId: String,
        senderUserId: String,
        message: String,
        inReplyToMessageId: String?,
    ): Result<SendMessageResponse> =
        runCatching {
            client.post {
                url {
                    path("chat/messages")
                    parameter("broadcaster_id", channelUserId)
                    parameter("sender_id", senderUserId)
                    parameter("message", message)
                    inReplyToMessageId?.let { id ->
                        parameter("reply_parent_message_id", id)
                    }
                }
            }.body()
        }
}
