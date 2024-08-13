package fr.outadoc.justchatting.feature.recent.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.UserQueries
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.recent.domain.LocalUsersApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

internal class LocalUsersDb(
    private val userQueries: UserQueries,
) : LocalUsersApi {

    override fun getAll(): Flow<List<User>> {
        return userQueries
            .getAllComplete()
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { channels ->
                channels.map { channel ->
                    User(
                        id = channel.id,
                        login = channel.id,
                        displayName = channel.id,
                        profileImageUrl = channel.profile_image_url,
                        description = channel.description,
                        createdAt = channel.created_at?.let { Instant.fromEpochMilliseconds(it) },
                        usedAt = channel.used_at?.let { Instant.fromEpochMilliseconds(it) },
                    )
                }
            }
    }

    override fun markAsVisited(userId: String, usedAt: Instant) {
        userQueries.transaction {
            userQueries.updateVisitedAt(
                id = userId,
                used_at = usedAt.toEpochMilliseconds(),
            )
        }
    }
}
