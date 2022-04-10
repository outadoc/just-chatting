package com.github.andreyasadchy.xtra.model.helix.video

import android.os.Parcelable
import com.github.andreyasadchy.xtra.model.offline.Downloadable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Video(
    override val id: String,
    val user_id: String? = null,
    val user_login: String? = null,
    val user_name: String? = null,
    override val title: String? = null,
    val description: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val thumbnail_url: String? = null,
    val view_count: Int? = null,
    override val type: String? = null,
    val duration: String? = null,
    @SerializedName("muted_segments")
    val mutedSegments: List<MutedSegment>? = null,

    override val gameId: String? = null,
    override val gameName: String? = null,
    var profileImageURL: String? = null) : Parcelable, Downloadable {

    @Parcelize
    data class MutedSegment(
            val duration: Int,
            val offset: Int) : Parcelable

        override val thumbnail: String?
                get() = TwitchApiHelper.getTemplateUrl(thumbnail_url, "video")
        override val channelId: String?
                get() = user_id
        override val channelLogin: String?
                get() = user_login
        override val channelName: String?
                get() = user_name
        override val channelLogo: String?
                get() = TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage")
        override val uploadDate: String?
                get() = createdAt
}