package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface LocalUsersApi {

    fun getUserById(id: String): Flow<User>
    fun getUsersById(ids: List<String>): Flow<List<User>>

    fun getRecentChannels(): Flow<List<User>>
    fun getFollowedChannels(): Flow<List<ChannelFollow>>

    fun getUserIdsToUpdate(): Flow<List<String>>

    suspend fun saveUser(userId: String, visitedAt: Instant? = null)
    suspend fun saveAndReplaceFollowedChannels(follows: List<ChannelFollow>)
    suspend fun saveUserInfo(users: List<User>)
}
