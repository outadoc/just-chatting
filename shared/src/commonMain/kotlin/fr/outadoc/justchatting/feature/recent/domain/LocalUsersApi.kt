package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface LocalUsersApi {

    fun getUserById(id: String): Flow<User>
    fun getUsersById(ids: List<String>): Flow<List<User>>
    fun getUserByLogin(login: String): Flow<User>
    fun getUsersByLogin(logins: List<String>): Flow<List<User>>

    fun getRecentChannels(): Flow<List<User>>

    fun getUserIdsToUpdate(): Flow<List<String>>

    fun rememberUser(userId: String, usedAt: Instant? = null, followedAt: Instant? = null)
    fun updateUserInfo(users: List<User>)
}
