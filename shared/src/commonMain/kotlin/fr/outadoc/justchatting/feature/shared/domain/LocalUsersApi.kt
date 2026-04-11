package fr.outadoc.justchatting.feature.shared.domain

import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

public interface LocalUsersApi {
    public fun getUserById(id: String): Flow<User>

    public fun getUsersById(ids: List<String>): Flow<List<User>>

    public fun getRecentChannels(): Flow<List<User>>

    public fun getFollowedChannels(): Flow<List<ChannelFollow>>

    public fun forgetRecentChannel(userId: String)

    public fun getUserIdsToUpdate(): Flow<List<String>>

    public suspend fun saveUser(
        userId: String,
        visitedAt: Instant? = null,
    )

    public suspend fun saveAndReplaceFollowedChannels(follows: List<ChannelFollow>)

    public suspend fun saveUserInfo(users: List<User>)

    public suspend fun isFollowedUsersCacheExpired(): Boolean
}
