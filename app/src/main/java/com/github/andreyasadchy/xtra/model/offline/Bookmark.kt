package com.github.andreyasadchy.xtra.model.offline

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey
    val id: String,
    val userId: String? = null,
    var userLogin: String? = null,
    var userName: String? = null,
    var userLogo: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val title: String? = null,
    val createdAt: String? = null,
    val thumbnail: String? = null,
    val type: String? = null,
    val duration: String? = null) : Parcelable
