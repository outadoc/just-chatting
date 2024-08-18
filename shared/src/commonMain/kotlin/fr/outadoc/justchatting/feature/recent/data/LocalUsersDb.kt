package fr.outadoc.justchatting.feature.recent.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.UserQueries
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.recent.domain.LocalUsersApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

internal class LocalUsersDb(
    private val userQueries: UserQueries,
    private val clock: Clock,
) : LocalUsersApi {

    override fun getRecentChannels(): Flow<List<User>> {
        return userQueries.getRecent()
            .asFlow()
            .distinctUntilChanged()
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
                        usedAt = if (userInfo.used_at > 0) {
                            Instant.fromEpochMilliseconds(userInfo.used_at)
                        } else {
                            null
                        },
                    )
                }
            }
            .flowOn(DispatchersProvider.io)
    }

    override fun getFollowedChannels(): Flow<List<ChannelFollow>> {
        return userQueries.getFollowed()
            .asFlow()
            .distinctUntilChanged()
            .mapToList(DispatchersProvider.io)
            .map { users ->
                users.map { userInfo ->
                    ChannelFollow(
                        user = User(
                            id = userInfo.id,
                            login = userInfo.login,
                            displayName = userInfo.display_name,
                            profileImageUrl = userInfo.profile_image_url,
                            description = userInfo.description,
                            createdAt = Instant.fromEpochMilliseconds(userInfo.created_at),
                            usedAt = if (userInfo.used_at > 0) {
                                Instant.fromEpochMilliseconds(userInfo.used_at)
                            } else {
                                null
                            },
                        ),
                        followedAt = Instant.fromEpochMilliseconds(userInfo.followed_at),
                    )
                }
            }
            .flowOn(DispatchersProvider.io)
    }

    override fun getUserById(id: String): Flow<User> {
        return getUsersById(listOf(id))
            .mapNotNull { user -> user.firstOrNull() }
    }

    override fun getUsersById(ids: List<String>): Flow<List<User>> {
        return userQueries.getByIds(ids)
            .asFlow()
            .distinctUntilChanged()
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
            .flowOn(DispatchersProvider.io)
    }

    override suspend fun saveUserInfo(users: List<User>) =
        withContext(DispatchersProvider.io) {
            val updatedAt = clock.now()
            userQueries.transaction {
                users.forEach { user ->
                    userQueries.ensureCreated(
                        id = user.id,
                        inserted_at = updatedAt.toEpochMilliseconds(),
                    )

                    userQueries.updateUserInfo(
                        id = user.id,
                        login = user.login,
                        display_name = user.displayName,
                        profile_image_url = user.profileImageUrl,
                        description = user.description,
                        created_at = user.createdAt.toEpochMilliseconds(),
                        updated_at = updatedAt.toEpochMilliseconds(),
                    )
                }
            }
        }

    override suspend fun saveUser(userId: String, visitedAt: Instant?) =
        withContext(DispatchersProvider.io) {
            val now = clock.now()
            userQueries.transaction {
                userQueries.ensureCreated(
                    id = userId,
                    inserted_at = now.toEpochMilliseconds(),
                )

                visitedAt?.let { usedAt ->
                    userQueries.updateVisitedAt(
                        id = userId,
                        used_at = usedAt.toEpochMilliseconds(),
                    )
                }
            }
        }

    override suspend fun saveAndReplaceFollowedChannels(follows: List<ChannelFollow>) =
        withContext(DispatchersProvider.io) {
            val now = clock.now()
            userQueries.transaction {
                follows.forEach { channelFollow ->
                    userQueries.ensureCreated(
                        id = channelFollow.user.id,
                        inserted_at = now.toEpochMilliseconds(),
                    )

                    userQueries.updateFollowedAt(
                        id = channelFollow.user.id,
                        followed_at = channelFollow.followedAt.toEpochMilliseconds(),
                    )
                }

                userQueries.setFollowedUsersUpdated(
                    last_updated = now.toEpochMilliseconds(),
                )
            }
        }

    override fun getUserIdsToUpdate(): Flow<List<String>> {
        val minAcceptableCacheDate = clock.now() - MaxUserCacheLife

        logDebug<LocalUsersDb> { "Updating users not updated after $minAcceptableCacheDate" }

        return userQueries
            .getAllToUpdate(
                minUpdatedAtTimestamp = minAcceptableCacheDate.toEpochMilliseconds(),
            )
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .flowOn(DispatchersProvider.io)
    }

    override suspend fun isFollowedUsersCacheExpired(): Boolean =
        withContext(DispatchersProvider.io) {
            val minAcceptableCacheDate = clock.now() - MaxFollowedUsersCacheLife
            val updatedAt = userQueries
                .getFollowedUsersUpdatedAt()
                .executeAsOneOrNull()

            updatedAt == null || updatedAt < minAcceptableCacheDate.toEpochMilliseconds()
        }

    private companion object {
        val MaxUserCacheLife = 1.days
        val MaxFollowedUsersCacheLife = 2.hours
    }
}
