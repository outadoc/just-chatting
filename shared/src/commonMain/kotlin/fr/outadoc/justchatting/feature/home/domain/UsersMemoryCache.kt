package fr.outadoc.justchatting.feature.home.domain

import fr.outadoc.justchatting.feature.home.domain.model.User

internal interface UsersMemoryCache {

    suspend fun getUsersById(ids: List<String>): List<User>

    suspend fun getUserById(id: String): User

    suspend fun getUsersByLogin(logins: List<String>): List<User>

    suspend fun getUserByLogin(login: String): User

    suspend fun put(users: List<User>)

    suspend fun put(user: User)
}
