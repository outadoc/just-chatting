package com.github.andreyasadchy.xtra.model.helix.game

import android.os.Parcelable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Game(
        val id: String,
        val name: String,
        val box_art_url: String,
        val viewersCount: Int = 0) : Parcelable {

        val boxArt: String
                get() = TwitchApiHelper.getTemplateUrl(box_art_url, "game")
}