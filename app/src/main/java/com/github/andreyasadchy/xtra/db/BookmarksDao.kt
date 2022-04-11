package com.github.andreyasadchy.xtra.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.andreyasadchy.xtra.model.offline.Bookmark

@Dao
interface BookmarksDao {

    @Query("SELECT * FROM bookmarks")
    fun getAll(): LiveData<List<Bookmark>>

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
