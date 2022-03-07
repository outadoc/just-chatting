package com.github.andreyasadchy.xtra.model.offline

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "local_follows")
data class LocalFollowChannel(
    @PrimaryKey
    val user_id: String,
    var user_login: String? = null,
    var user_name: String? = null,
    var channelLogo: String? = null) : Parcelable
