package fr.outadoc.justchatting.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.outadoc.justchatting.model.offline.Request

@Dao
interface RequestsDao {

    @Query("SELECT * FROM requests")
    suspend fun getAll(): List<Request>

    @Insert
    suspend fun insert(request: Request)

    @Delete
    suspend fun delete(request: Request)
}
