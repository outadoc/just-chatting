package fr.outadoc.justchatting.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.outadoc.justchatting.model.offline.OfflineVideo

@Dao
interface VideosDao {

    @Query("SELECT * FROM videos ORDER BY id DESC")
    fun getAll(): LiveData<List<OfflineVideo>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getById(id: Int): OfflineVideo?

    @Query("SELECT * FROM videos WHERE channel_id = :id")
    fun getByUserId(id: Int): List<OfflineVideo>

    @Insert
    fun insert(video: OfflineVideo): Long

    @Delete
    fun delete(video: OfflineVideo)

    @Update
    fun update(video: OfflineVideo)

    @Query("UPDATE videos SET last_watch_position = :position WHERE id = :id")
    fun updatePosition(id: Int, position: Long)

    @Query("UPDATE videos SET last_watch_position = null")
    fun deletePositions()
}
