package fr.outadoc.justchatting.feature.demo.data

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepositoryImpl
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class DemoAwareTwitchRepository(
    private val demoModeRepository: DemoModeRepository,
    private val real: Lazy<TwitchRepositoryImpl>,
    private val demo: DemoTwitchRepository,
) : TwitchRepository {
    private fun current(): TwitchRepository = if (demoModeRepository.isDemoMode.value) demo else real.value

    private fun <T> liveFlow(block: suspend (TwitchRepository) -> Flow<T>): Flow<T> =
        demoModeRepository.isDemoMode.flatMapLatest { isDemoMode ->
            block(if (isDemoMode) demo else real.value)
        }

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> =
        liveFlow { repository -> repository.searchChannels(query) }

    override suspend fun getFollowedChannels(): Flow<List<ChannelFollow>> = liveFlow { repository -> repository.getFollowedChannels() }

    override suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>> =
        liveFlow { repository -> repository.getStreamByUserId(userId) }

    override suspend fun getUserById(id: String): Flow<Result<User>> = liveFlow { repository -> repository.getUserById(id) }

    override suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>> =
        liveFlow { repository -> repository.getUsersById(ids) }

    override suspend fun getCheerEmotes(userId: String): Result<List<Emote>> = current().getCheerEmotes(userId)

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>> = current().getEmotesFromSet(setIds)

    override suspend fun getRecentChannels(): Flow<List<User>> = liveFlow { repository -> repository.getRecentChannels() }

    override suspend fun forgetRecentChannel(userId: String) {
        current().forgetRecentChannel(userId)
    }

    override suspend fun getFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
    ): Flow<FullSchedule> = liveFlow { repository -> repository.getFollowedChannelsSchedule(today, timeZone) }

    override suspend fun markChannelAsVisited(
        userId: String,
        visitedAt: Instant,
    ) {
        current().markChannelAsVisited(userId, visitedAt)
    }

    override suspend fun getGlobalBadges(): Result<List<TwitchBadge>> = current().getGlobalBadges()

    override suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>> = current().getChannelBadges(channelId)

    override suspend fun sendChatMessage(
        channelUserId: String,
        message: String,
        inReplyToMessageId: String?,
        appUser: AppUser,
    ): Result<String> = current().sendChatMessage(channelUserId, message, inReplyToMessageId, appUser)

    override suspend fun syncFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
        appUser: AppUser,
    ) {
        current().syncFollowedChannelsSchedule(today, timeZone, appUser)
    }

    override suspend fun syncFollowedStreams(appUser: AppUser) {
        current().syncFollowedStreams(appUser)
    }

    override suspend fun syncFollowedChannels(appUser: AppUser) {
        current().syncFollowedChannels(appUser)
    }
}
