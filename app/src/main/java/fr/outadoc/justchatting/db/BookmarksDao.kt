package fr.outadoc.justchatting.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.outadoc.justchatting.model.offline.Bookmark

@Dao
interface BookmarksDao {

    @Query("SELECT * FROM bookmarks")
    fun getAllLiveData(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmarks")
    fun getAll(): List<Bookmark>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    fun getById(id: String): Bookmark?

    @Query("SELECT * FROM bookmarks WHERE userId = :id")
    fun getByUserId(id: String): List<Bookmark>

    @Insert
    fun insert(video: Bookmark)

    @Delete
    fun delete(video: Bookmark)

    @Update
    fun update(video: Bookmark)
}
