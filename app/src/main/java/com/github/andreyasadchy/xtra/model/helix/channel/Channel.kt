package com.github.andreyasadchy.xtra.model.helix.channel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Channel(
        val id: String,
        val broadcaster_login: String,
        val display_name: String,
        val game_id: String = "",
        val game_name: String = "",
        val is_live: Boolean = false,
        val title: String = "",
        val started_at: String = "",
        val broadcaster_language: String = "",
        val thumbnail_url: String = "",
        var profileImageURL: String = "") : Parcelable