package fr.outadoc.justchatting.feature.home.domain

import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.withContext

internal class UsersMemoryCacheImpl : UsersMemoryCache {

    private var userCacheByLogin: PersistentMap<String, User> = persistentMapOf()
    private var userCacheById: PersistentMap<String, User> = persistentMapOf()

    override suspend fun getUsersById(ids: List<String>): List<User> {
        return ids
            .mapNotNull { id -> userCacheById[id] }
            .onEach { user ->
                logDebug<UsersMemoryCacheImpl> { "got $user from cache" }
            }
    }

    override suspend fun getUsersByLogin(logins: List<String>): List<User> {
        return logins
            .mapNotNull { login -> userCacheByLogin[login] }
            .onEach { user ->
                logDebug<UsersMemoryCacheImpl> { "got $user from cache" }
            }
    }

    override suspend fun put(users: List<User>) {
        withContext(DispatchersProvider.default) {
            logDebug<UsersMemoryCacheImpl> { "put: writing $users to cache" }
            userCacheById = userCacheById.putAll(users.associateBy { it.id })
            userCacheByLogin = userCacheByLogin.putAll(users.associateBy { it.login })
        }
    }

    override suspend fun put(user: User) {
        withContext(DispatchersProvider.default) {
            logDebug<UsersMemoryCacheImpl> { "put: writing $user to cache" }
            userCacheById = userCacheById.put(user.id, user)
            userCacheByLogin = userCacheByLogin.put(user.login, user)
        }
    }
}