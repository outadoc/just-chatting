package fr.outadoc.justchatting.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.outadoc.justchatting.model.offline.SortChannel

@Dao
interface SortChannelDao {

    @Query("SELECT * FROM sort_channel WHERE id = :id")
    fun getById(id: String): SortChannel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(video: SortChannel)
}
