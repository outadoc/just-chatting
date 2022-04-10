package com.github.andreyasadchy.xtra.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.andreyasadchy.xtra.model.offline.VodBookmarkIgnoredUser

@Dao
interface VodBookmarkIgnoredUsersDao {

    @Query("SELECT * FROM vod_bookmark_ignored_users")
    fun getAll(): LiveData<List<VodBookmarkIgnoredUser>>

    @Query("SELECT * FROM vod_bookmark_ignored_users WHERE user_id = :id")
    fun getById(id: String): VodBookmarkIgnoredUser?

    @Insert
    fun insert(user: VodBookmarkIgnoredUser)

    @Delete
    fun delete(user: VodBookmarkIgnoredUser)
}
