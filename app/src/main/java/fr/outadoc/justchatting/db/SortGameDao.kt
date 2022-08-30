package fr.outadoc.justchatting.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.outadoc.justchatting.model.offline.SortGame

@Dao
interface SortGameDao {

    @Query("SELECT * FROM sort_game WHERE id = :id")
    fun getById(id: String): SortGame?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(video: SortGame)
}
