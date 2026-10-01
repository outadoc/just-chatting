package fr.outadoc.justchatting.feature.shared.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.chat.domain.model.SharedChatSession
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

internal interface TwitchRepository {
    suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>>

    suspend fun getFollowedChannels(): Flow<List<ChannelFollow>>

    suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>>

    suspend fun getUserById(id: String): Flow<Result<User>>

    suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>>

    suspend fun getCheerEmotes(userId: String): Result<List<Emote>>

    suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>>

    suspend fun getRecentChannels(): Flow<List<User>>

    suspend fun forgetRecentChannel(userId: String)

    suspend fun getFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
    ): Flow<FullSchedule>

    suspend fun markChannelAsVisited(
        userId: String,
        visitedAt: Instant,
    )

    suspend fun getGlobalBadges(): Result<List<TwitchBadge>>

    suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>>

    /**
     * @return the channel's active shared chat session, or null if it isn't in one.
     */
    suspend fun getSharedChatSession(channelId: String): Result<SharedChatSession?>

    suspend fun sendChatMessage(
        channelUserId: String,
        message: String,
        inReplyToMessageId: String?,
        appUser: AppUser,
    ): Result<String>

    suspend fun syncFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
        appUser: AppUser,
    )

    suspend fun syncFollowedStreams(appUser: AppUser)

    suspend fun syncFollowedChannels(appUser: AppUser)
}
