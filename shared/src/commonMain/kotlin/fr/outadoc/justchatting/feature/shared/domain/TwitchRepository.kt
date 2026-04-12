package fr.outadoc.justchatting.feature.shared.domain

import androidx.paging.PagingData
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

public interface TwitchRepository {
    public suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>>

    public suspend fun getFollowedChannels(): Flow<List<ChannelFollow>>

    public suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>>

    public suspend fun getUserById(id: String): Flow<Result<User>>

    public suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>>

    public suspend fun getCheerEmotes(userId: String): Result<List<Emote>>

    public suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>>

    public suspend fun getRecentChannels(): Flow<List<User>>

    public suspend fun forgetRecentChannel(userId: String)

    public suspend fun getFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
    ): Flow<FullSchedule>

    public suspend fun markChannelAsVisited(
        userId: String,
        visitedAt: Instant,
    )

    public suspend fun getGlobalBadges(): Result<List<TwitchBadge>>

    public suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>>

    public suspend fun sendChatMessage(
        channelUserId: String,
        message: String,
        inReplyToMessageId: String?,
        appUser: AppUser,
    ): Result<String>

    public suspend fun syncFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
        appUser: AppUser,
    )

    public suspend fun syncFollowedStreams(appUser: AppUser)

    public suspend fun syncFollowedChannels(appUser: AppUser)
}
