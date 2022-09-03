package fr.outadoc.justchatting.model.helix.user

import android.os.Parcelable
import fr.outadoc.justchatting.util.getTemplateUrl
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val login: String,
    val display_name: String,
    val description: String? = null,
    val profile_image_url: String? = null,
    val offline_image_url: String? = null,
    val created_at: String? = null,
    val followers_count: Int? = null,
    val bannerImageURL: String? = null
) : Parcelable {

    val channelLogo: String?
        get() = getTemplateUrl(profile_image_url, "profileimage")
}
