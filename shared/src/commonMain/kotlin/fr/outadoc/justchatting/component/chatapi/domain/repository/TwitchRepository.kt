package fr.outadoc.justchatting.component.chatapi.domain.repository

import androidx.paging.PagingData
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSchedule
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface TwitchRepository {
    suspend fun loadSearchChannels(query: String): Flow<PagingData<ChannelSearch>>
    suspend fun loadFollowedStreams(): Flow<PagingData<Stream>>
    suspend fun loadFollowedChannels(): Flow<PagingData<ChannelFollow>>
    suspend fun loadStream(userId: String): Result<Stream>
    suspend fun loadUsersById(ids: List<String>): Result<List<User>>
    suspend fun loadUsersByLogin(logins: List<String>): Result<List<User>>
    suspend fun loadCheerEmotes(userId: String): Result<List<Emote>>
    suspend fun loadEmotesFromSet(setIds: List<String>): Result<List<Emote>>
    suspend fun getRecentChannels(): Flow<List<ChannelSearch>?>
    suspend fun insertRecentChannel(channel: User, usedAt: Instant)
    suspend fun loadChannelSchedule(channelId: String): Result<ChannelSchedule>
}
