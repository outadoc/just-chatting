package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface TwitchRepository {
    suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>>
    suspend fun getFollowedStreams(): Flow<PagingData<Stream>>
    suspend fun getFollowedChannels(): Flow<PagingData<ChannelFollow>>
    suspend fun getStream(userId: String): Result<Stream>
    suspend fun getUsersById(ids: List<String>): Result<List<User>>
    suspend fun getUserByLogin(login: String): Result<User>
    suspend fun getUsersByLogin(logins: List<String>): Result<List<User>>
    suspend fun getCheerEmotes(userId: String): Result<List<Emote>>
    suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>>
    suspend fun getRecentChannels(): Flow<List<ChannelSearchResult>?>
    suspend fun insertRecentChannel(channel: User, usedAt: Instant)
    suspend fun loadChannelSchedule(channelId: String): Result<ChannelSchedule>
    suspend fun loadGlobalBadges(): Result<List<TwitchBadge>>
    suspend fun loadChannelBadges(channelId: String): Result<List<TwitchBadge>>
}
