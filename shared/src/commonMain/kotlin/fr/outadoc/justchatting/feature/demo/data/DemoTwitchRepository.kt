package fr.outadoc.justchatting.feature.demo.data

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class DemoTwitchRepository(
    private val clock: Clock,
    private val demoChatBus: DemoChatBus,
) : TwitchRepository {
    private val _recentChannels = MutableStateFlow(DemoData.channels.take(1))
    private val recentChannels: StateFlow<List<User>> = _recentChannels

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> =
        flowOf(
            PagingData.from(
                data = DemoData.searchResults.filter { it.user.displayName.contains(query, ignoreCase = true) },
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            ),
        )

    override suspend fun getFollowedChannels(): Flow<List<ChannelFollow>> = flowOf(DemoData.follows)

    override suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>> =
        flowOf(
            DemoData.liveStreams
                .firstOrNull { it.user.id == userId }
                ?.stream
                ?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("No live stream for user $userId")),
        )

    override suspend fun getUserById(id: String): Flow<Result<User>> = flowOf(Result.success(findOrSynthesizeUser(id)))

    override suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>> =
        flowOf(Result.success(ids.map { id -> findOrSynthesizeUser(id) }))

    override suspend fun getCheerEmotes(userId: String): Result<List<Emote>> = Result.success(DemoData.cheerEmotes)

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>> = Result.success(DemoData.setEmotes)

    override suspend fun getRecentChannels(): Flow<List<User>> = recentChannels

    override suspend fun forgetRecentChannel(userId: String) {
        _recentChannels.value = _recentChannels.value.filterNot { it.id == userId }
    }

    override suspend fun getFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
    ): Flow<FullSchedule> =
        flowOf(
            FullSchedule(
                past = persistentListOf(),
                live = DemoData.liveStreams.toPersistentList(),
                future = DemoData.futureSchedule.toImmutableList(),
            ),
        )

    override suspend fun markChannelAsVisited(
        userId: String,
        visitedAt: Instant,
    ) {
        val user = findOrSynthesizeUser(userId)
        _recentChannels.value = (listOf(user) + _recentChannels.value.filterNot { it.id == userId }).take(10)
    }

    override suspend fun getGlobalBadges(): Result<List<TwitchBadge>> = Result.success(DemoData.globalBadges)

    override suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>> = Result.success(DemoData.channelBadges)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun sendChatMessage(
        channelUserId: String,
        message: String,
        inReplyToMessageId: String?,
        appUser: AppUser,
    ): Result<String> {
        val messageId = Uuid.random().toString()

        demoChatBus.post(
            channelId = channelUserId,
            event =
                ChatEvent.Message.ChatMessage(
                    timestamp = clock.now(),
                    id = messageId,
                    userId = DemoData.currentUser.id,
                    userLogin = DemoData.currentUser.login,
                    userName = DemoData.currentUser.displayName,
                    message = message,
                    color = null,
                    embeddedEmotes = emptyList(),
                    badges = emptyList<Badge>(),
                    rewardId = null,
                    inReplyTo = null,
                ),
        )

        return Result.success(messageId)
    }

    override suspend fun syncFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
        appUser: AppUser,
    ) {
        // No-op: demo data is static.
    }

    override suspend fun syncFollowedStreams(appUser: AppUser) {
        // No-op: demo data is static.
    }

    override suspend fun syncFollowedChannels(appUser: AppUser) {
        // No-op: demo data is static.
    }

    private fun findOrSynthesizeUser(id: String): User = DemoData.findUser(id) ?: DemoData.syntheticUser(id)
}
