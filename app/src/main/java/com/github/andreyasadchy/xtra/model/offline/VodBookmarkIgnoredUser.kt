package com.github.andreyasadchy.xtra.model.offline

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "vod_bookmark_ignored_users")
data class VodBookmarkIgnoredUser(
    @PrimaryKey
    val user_id: String) : Parcelable
