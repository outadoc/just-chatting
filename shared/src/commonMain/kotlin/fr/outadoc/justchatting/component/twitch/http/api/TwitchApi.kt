package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.ChannelScheduleResponse
import fr.outadoc.justchatting.component.twitch.http.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.http.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.http.model.FollowResponse
import fr.outadoc.justchatting.component.twitch.http.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.http.model.TwitchBadgesResponse
import fr.outadoc.justchatting.component.twitch.http.model.UsersResponse
import fr.outadoc.justchatting.feature.emotes.data.twitch.model.EmoteSetResponse

internal interface TwitchApi {

    suspend fun getStreams(ids: List<String>): Result<StreamsResponse>

    suspend fun getFollowedStreams(
        userId: String?,
        limit: Int,
        after: String? = null,
    ): Result<StreamsResponse>

    suspend fun getUsersById(ids: List<String>): Result<UsersResponse>

    suspend fun getUsersByLogin(logins: List<String>): Result<UsersResponse>

    suspend fun searchChannels(
        query: String,
        limit: Int,
        after: String?,
    ): Result<ChannelSearchResponse>

    suspend fun getFollowedChannels(
        userId: String?,
        limit: Int,
        after: String? = null,
    ): Result<FollowResponse>

    suspend fun getEmotesFromSet(setIds: List<String>): Result<EmoteSetResponse>

    suspend fun getCheerEmotes(userId: String?): Result<CheerEmotesResponse>

    suspend fun getGlobalBadges(): Result<TwitchBadgesResponse>

    suspend fun getChannelBadges(channelId: String): Result<TwitchBadgesResponse>

    suspend fun getChannelSchedule(
        channelId: String,
        limit: Int,
        after: String?,
    ): Result<ChannelScheduleResponse>
}
