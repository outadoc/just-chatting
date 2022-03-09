package com.github.andreyasadchy.xtra.model.offline

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "local_follows_games")
data class LocalFollowGame(
    @PrimaryKey
    val game_id: String,
    var game_name: String? = null,
    var boxArt: String? = null) : Parcelable
