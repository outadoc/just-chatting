package fr.outadoc.justchatting.model.helix.channel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChannelSearch(
    val id: String? = null,
    val broadcaster_login: String? = null,
    val display_name: String? = null,
    val game_id: String? = null,
    val game_name: String? = null,
    val is_live: Boolean = false,
    val title: String? = null,
    val started_at: String? = null,
    val broadcaster_language: String? = null,
    val thumbnail_url: String? = null,
    val profileImageURL: String? = null,
    val followers_count: Int? = null
) : Parcelable
