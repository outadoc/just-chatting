package com.github.andreyasadchy.xtra.db

import androidx.room.*
import com.github.andreyasadchy.xtra.model.offline.LocalFollow

@Dao
interface LocalFollowsDao {

    @Query("SELECT * FROM local_follows")
    fun getAll(): List<LocalFollow>

    @Query("SELECT * FROM local_follows WHERE user_id = :id")
    fun getById(id: String): LocalFollow?

    @Insert
    fun insert(video: LocalFollow)

    @Delete
    fun delete(video: LocalFollow)

    @Update
    fun update(video: LocalFollow)
}
