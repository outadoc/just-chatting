package fr.outadoc.justchatting.feature.recent.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.UserQueries
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.recent.domain.LocalUsersApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Instant

internal class LocalUsersDb(
    private val userQueries: UserQueries,
) : LocalUsersApi {

    override fun getRecentChannels(): Flow<List<User>> {
        return userQueries.getRecent()
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { users ->
                users.map { userInfo ->
                    User(
                        id = userInfo.id,
                        login = userInfo.login,
                        displayName = userInfo.display_name,
                        profileImageUrl = userInfo.profile_image_url,
                        description = userInfo.description,
                        createdAt = Instant.fromEpochMilliseconds(userInfo.created_at),
                        usedAt = Instant.fromEpochMilliseconds(userInfo.used_at),
                    )
                }
            }
    }

    override fun getUserById(id: String): Flow<User> {
        return getUsersById(listOf(id))
            .mapNotNull { user -> user.firstOrNull() }
    }

    override fun getUsersById(ids: List<String>): Flow<List<User>> {
        return userQueries.getByIds(ids)
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { users ->
                users.map { userInfo ->
                    User(
                        id = userInfo.id,
                        login = userInfo.login,
                        displayName = userInfo.display_name,
                        profileImageUrl = userInfo.profile_image_url,
                        description = userInfo.description,
                        createdAt = Instant.fromEpochMilliseconds(userInfo.created_at),
                        usedAt = Instant.fromEpochMilliseconds(userInfo.used_at),
                    )
                }
            }
    }

    override fun getUserByLogin(login: String): Flow<User> {
        return getUsersByLogin(listOf(login))
            .mapNotNull { user -> user.firstOrNull() }
    }

    override fun getUsersByLogin(logins: List<String>): Flow<List<User>> {
        return userQueries.getByLogins(logins)
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .mapNotNull { users ->
                users.map { userInfo ->
                    User(
                        id = userInfo.id,
                        login = userInfo.login,
                        displayName = userInfo.display_name,
                        profileImageUrl = userInfo.profile_image_url,
                        description = userInfo.description,
                        createdAt = Instant.fromEpochMilliseconds(userInfo.created_at),
                        usedAt = Instant.fromEpochMilliseconds(userInfo.used_at),
                    )
                }
            }
    }

    override fun rememberUser(userId: String, usedAt: Instant?, followedAt: Instant?) {
        userQueries.transaction {
            userQueries.createUser(userId)

            usedAt?.let { usedAt ->
                userQueries.updateVisitedAt(
                    id = userId,
                    used_at = usedAt.toEpochMilliseconds(),
                )
            }

            followedAt?.let { followedAt ->
                userQueries.updateFollowedAt(
                    id = userId,
                    followed_at = followedAt.toEpochMilliseconds(),
                )
            }
        }
    }
}
