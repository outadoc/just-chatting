package fr.outadoc.justchatting.component.chatapi.domain.repository

import androidx.paging.PagingData
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import kotlinx.coroutines.flow.Flow

interface TwitchRepository {

    suspend fun loadSearchChannels(query: String): Flow<PagingData<ChannelSearch>>
    suspend fun loadFollowedStreams(): Flow<PagingData<Stream>>
    suspend fun loadFollowedChannels(): Flow<PagingData<ChannelFollow>>
    suspend fun loadStream(userId: String): Stream?
    suspend fun loadUsersById(ids: List<String>): List<User>?
    suspend fun loadUsersByLogin(logins: List<String>): List<User>?
    suspend fun loadCheerEmotes(userId: String): List<Emote>
    suspend fun loadEmotesFromSet(setIds: List<String>): List<Emote>?
}
