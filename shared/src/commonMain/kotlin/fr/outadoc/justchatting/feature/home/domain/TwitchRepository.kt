package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

internal interface TwitchRepository {

    suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>>

    suspend fun getFollowedChannels(): Flow<List<ChannelFollow>>

    suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>>

    suspend fun getUserById(id: String): Flow<Result<User>>

    suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>>

    suspend fun getCheerEmotes(userId: String): Result<List<Emote>>

    suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>>

    suspend fun getRecentChannels(): Flow<List<ChannelSearchResult>?>

    suspend fun markChannelAsVisited(channel: User, visitedAt: Instant)

    suspend fun syncFollowedChannelsSchedule(today: LocalDate, timeZone: TimeZone)

    suspend fun getFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
    ): Flow<FullSchedule>

    suspend fun getGlobalBadges(): Result<List<TwitchBadge>>

    suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>>
}
