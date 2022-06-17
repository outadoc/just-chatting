package com.github.andreyasadchy.xtra.model.helix.follows

import android.os.Parcelable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Follow(
    val from_id: String? = null,
    val from_login: String? = null,
    val from_name: String? = null,
    val to_id: String? = null,
    val to_login: String? = null,
    val to_name: String? = null,
    val followed_at: String? = null,

    val profileImageURL: String? = null,
    val lastBroadcast: String? = null,

    val followTwitch: Boolean = false,
    val followLocal: Boolean = false
) : Parcelable {

    val channelLogo: String?
        get() = TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage")
}
