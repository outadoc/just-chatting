package com.github.andreyasadchy.xtra.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.andreyasadchy.xtra.model.offline.LocalFollowChannel

@Dao
interface LocalFollowsChannelDao {

    @Query("SELECT * FROM local_follows")
    fun getAll(): List<LocalFollowChannel>

    @Query("SELECT * FROM local_follows WHERE user_id = :id")
    fun getById(id: String): LocalFollowChannel?

    @Insert
    fun insert(video: LocalFollowChannel)

    @Delete
    fun delete(video: LocalFollowChannel)

    @Update
    fun update(video: LocalFollowChannel)
}
