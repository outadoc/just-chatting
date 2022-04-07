package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.db.VodBookmarkIgnoredUsersDao
import com.github.andreyasadchy.xtra.model.offline.VodBookmarkIgnoredUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VodBookmarkIgnoredUsersRepository @Inject constructor(
    private val vodBookmarkIgnoredUsersDao: VodBookmarkIgnoredUsersDao) {

    fun loadUsers() = vodBookmarkIgnoredUsersDao.getAll()

    suspend fun getUserById(id: String) = withContext(Dispatchers.IO) {
        vodBookmarkIgnoredUsersDao.getById(id)
    }

    suspend fun saveUser(item: VodBookmarkIgnoredUser) = withContext(Dispatchers.IO) {
        vodBookmarkIgnoredUsersDao.insert(item)
    }

    fun deleteUser(item: VodBookmarkIgnoredUser) {
        GlobalScope.launch { vodBookmarkIgnoredUsersDao.delete(item) }
    }
}
