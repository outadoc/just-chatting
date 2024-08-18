package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.home.domain.model.UserStream
import fr.outadoc.justchatting.feature.home.domain.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface TwitchApi {

    suspend fun getStreamsByUserId(ids: List<String>): Result<List<Stream>>

    suspend fun getStreamsByUserLogin(logins: List<String>): Result<List<Stream>>

    suspend fun getUsersById(ids: List<String>): List<User>

    suspend fun getUsersByLogin(logins: List<String>): List<User>

    suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>>

    suspend fun getCheerEmotes(userId: String?): Result<List<Emote>>

    suspend fun getGlobalBadges(): Result<List<TwitchBadge>>

    suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>>

    suspend fun getChannelVideos(
        channelId: String,
        notBefore: Instant,
    ): Result<List<Video>>

    suspend fun getChannelSchedule(
        userId: String,
        notBefore: Instant,
        notAfter: Instant,
    ): Result<List<ChannelScheduleSegment>>

    suspend fun getFollowedChannels(userId: String): Result<List<ChannelFollow>>

    suspend fun getFollowedStreamsOnline(userId: String): Flow<PagingData<List<Stream>>>

    suspend fun getFollowedStreams(userId: String): Result<List<Stream>>

    suspend fun searchChannels(query: String): Flow<PagingData<List<ChannelSearchResult>>>
}
