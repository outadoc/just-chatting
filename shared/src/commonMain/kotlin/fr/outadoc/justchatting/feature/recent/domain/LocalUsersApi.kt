package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface LocalUsersApi {
    fun getAll(): Flow<List<User>>
    fun markAsVisited(userId: String, usedAt: Instant)
}
