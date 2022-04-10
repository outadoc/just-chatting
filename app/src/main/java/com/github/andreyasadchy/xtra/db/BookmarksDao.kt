package com.github.andreyasadchy.xtra.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.andreyasadchy.xtra.model.offline.Bookmark

@Dao
interface BookmarksDao {

    @Query("SELECT * FROM bookmarks ORDER BY id DESC")
    fun getAll(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    fun getById(id: String): Bookmark?

    @Insert
    fun insert(user: Bookmark)

    @Delete
    fun delete(user: Bookmark)
}
