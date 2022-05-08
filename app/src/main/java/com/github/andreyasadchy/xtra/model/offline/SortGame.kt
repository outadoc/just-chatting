package com.github.andreyasadchy.xtra.model.offline

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "sort_game")
data class SortGame(
    @PrimaryKey
    val id: String,
    var saveSort: Boolean? = null,
    var videoSort: String? = null,
    var videoPeriod: String? = null,
    var videoType: String? = null,
    var videoLanguageIndex: Int? = null,
    var clipPeriod: String? = null,
    var clipLanguageIndex: Int? = null) : Parcelable
