package com.github.andreyasadchy.xtra.db

import androidx.room.*
import com.github.andreyasadchy.xtra.model.offline.LocalFollowGame

@Dao
interface LocalFollowsGameDao {

    @Query("SELECT * FROM local_follows_games")
    fun getAll(): List<LocalFollowGame>

    @Query("SELECT * FROM local_follows_games WHERE game_id = :id")
    fun getById(id: String): LocalFollowGame?

    @Insert
    fun insert(video: LocalFollowGame)

    @Delete
    fun delete(video: LocalFollowGame)

    @Update
    fun update(video: LocalFollowGame)
}
