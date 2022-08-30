package fr.outadoc.justchatting.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.outadoc.justchatting.model.VideoPosition

@Dao
interface VideoPositionsDao {

    @Query("SELECT * FROM video_positions")
    fun getAll(): LiveData<List<VideoPosition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(position: VideoPosition)

    @Query("DELETE FROM video_positions")
    fun deleteAll()
}
