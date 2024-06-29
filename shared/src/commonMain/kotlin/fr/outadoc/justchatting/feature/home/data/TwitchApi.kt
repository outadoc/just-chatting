package fr.outadoc.justchatting.feature.home.data

import fr.outadoc.justchatting.feature.emotes.data.twitch.model.EmoteSetResponse
import fr.outadoc.justchatting.feature.home.data.model.ChannelScheduleResponse
import fr.outadoc.justchatting.feature.home.data.model.ChannelSearchResponse
import fr.outadoc.justchatting.feature.home.data.model.CheerEmotesResponse
import fr.outadoc.justchatting.feature.home.data.model.FollowResponse
import fr.outadoc.justchatting.feature.home.data.model.StreamsResponse
import fr.outadoc.justchatting.feature.home.data.model.TwitchBadgesResponse
import fr.outadoc.justchatting.feature.home.data.model.UsersResponse

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
