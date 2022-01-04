package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.db.LocalFollowsDao
import com.github.andreyasadchy.xtra.model.offline.LocalFollow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFollowRepository @Inject constructor(
        private val localFollows: LocalFollowsDao) {

    fun loadFollows() = localFollows.getAll()

    suspend fun getFollowById(id: String) = withContext(Dispatchers.IO) {
        localFollows.getById(id)
    }

    suspend fun saveFollow(item: LocalFollow) = withContext(Dispatchers.IO) {
        localFollows.insert(item)
    }

    fun deleteFollow(item: LocalFollow) {
        GlobalScope.launch { localFollows.delete(item) }
    }

    fun updateFollow(item: LocalFollow) {
        GlobalScope.launch { localFollows.update(item) }
    }
}
