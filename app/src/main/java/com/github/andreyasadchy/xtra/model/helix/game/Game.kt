package com.github.andreyasadchy.xtra.model.helix.game

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Game(
        val id: String,
        val name: String,
        val box_art_url: String) : Parcelable