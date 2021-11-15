package com.github.andreyasadchy.xtra.model.helix.clip

import android.os.Parcelable
import com.github.andreyasadchy.xtra.model.offline.Downloadable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Clip(
        override val id: String,
        val url: String,
        val embed_url: String,
        val broadcaster_id: String,
        val broadcaster_name: String,
        val creator_id: String,
        val creator_name: String,
        val video_id: String,
        val game_id: String,
        override val title: String,
        val view_count: Int,
        val created_at: String,
        val thumbnail_url: String,
        val duration: Double) : Parcelable, Downloadable {

        override val thumbnail: String
                get() = thumbnail_url
        override val channelName: String
                get() = broadcaster_name
        override val channelLogo: String
                get() = ""
        override val game: String
                get() = game_id
        override val uploadDate: String
                get() = created_at
}
