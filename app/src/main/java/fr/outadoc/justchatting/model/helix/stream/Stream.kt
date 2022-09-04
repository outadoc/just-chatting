package fr.outadoc.justchatting.model.helix.stream

import android.os.Parcelable
import fr.outadoc.justchatting.model.helix.tag.Tag
import fr.outadoc.justchatting.model.helix.user.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stream(
    val id: String? = null,
    val user_id: String? = null,
    val user_login: String? = null,
    val user_name: String? = null,
    val game_id: String? = null,
    val game_name: String? = null,
    val type: String? = null,
    val title: String? = null,
    val viewer_count: Int? = null,
    val started_at: String? = null,
    val profileImageURL: String? = null,
    val tags: List<Tag>? = null,
    val channelUser: User? = null,
    val lastBroadcast: String? = null
) : Parcelable
