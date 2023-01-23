package fr.outadoc.justchatting.component.chatapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Follow(
    val fromId: String? = null,
    val fromLogin: String? = null,
    val fromName: String? = null,
    val toId: String? = null,
    val toLogin: String? = null,
    val toName: String? = null,
    val followedAt: String? = null,

    val profileImageURL: String? = null
) : Parcelable
