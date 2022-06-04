package com.github.andreyasadchy.xtra.model.helix.game

import android.os.Parcelable
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Game(
    val id: String? = null,
    val name: String? = null,
    val box_art_url: String? = null,

    var viewersCount: Int? = null,
    var broadcastersCount: Int? = null,
    var tags: List<Tag>? = null,
    val vodPosition: Int? = null,
    val vodDuration: Int? = null,

    var followTwitch: Boolean = false,
    val followLocal: Boolean = false
) : Parcelable {

    val boxArt: String?
        get() = TwitchApiHelper.getTemplateUrl(box_art_url, "game")
}
