package fr.outadoc.justchatting.feature.shared.domain

import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

internal interface LocalUsersApi {
    fun getUserById(id: String): Flow<User>

    fun getUsersById(ids: List<String>): Flow<List<User>>

    fun getRecentChannels(): Flow<List<User>>

    fun getFollowedChannels(): Flow<List<ChannelFollow>>

    fun forgetRecentChannel(userId: String)

    fun getUserIdsToUpdate(): Flow<List<String>>

    suspend fun saveUser(
        userId: String,
        visitedAt: Instant? = null,
    )

    suspend fun saveAndReplaceFollowedChannels(follows: List<ChannelFollow>)

    suspend fun saveUserInfo(users: List<User>)

    suspend fun isFollowedUsersCacheExpired(): Boolean
}
