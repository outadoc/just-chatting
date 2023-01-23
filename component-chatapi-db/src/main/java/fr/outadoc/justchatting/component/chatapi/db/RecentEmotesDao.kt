package fr.outadoc.justchatting.component.chatapi.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentEmotesDao {

    @Query("SELECT * FROM recent_emotes ORDER BY used_at DESC")
    fun getAll(): Flow<List<RecentEmote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(emotes: Collection<RecentEmote>)

    @Query("DELETE FROM recent_emotes WHERE name NOT IN (SELECT name FROM recent_emotes ORDER BY used_at DESC LIMIT ${MaxRecentEmotes})")
    fun deleteOld()

    @Transaction
    fun ensureMaxSizeAndInsert(emotes: Collection<RecentEmote>) {
        insertAll(emotes)
        deleteOld()
    }
}