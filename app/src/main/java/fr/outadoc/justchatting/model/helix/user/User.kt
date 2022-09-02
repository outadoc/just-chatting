package fr.outadoc.justchatting.model.helix.user

import android.os.Parcelable
import fr.outadoc.justchatting.util.getTemplateUrl
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String? = null,
    val login: String? = null,
    val display_name: String? = null,
    val type: String? = null,
    val broadcaster_type: String? = null,
    val description: String? = null,
    val profile_image_url: String? = null,
    val offline_image_url: String? = null,
    val view_count: Int? = null,
    val created_at: String? = null,
    val followers_count: Int? = null,
    val bannerImageURL: String? = null
) : Parcelable {

    val channelLogo: String?
        get() = getTemplateUrl(profile_image_url, "profileimage")
}
