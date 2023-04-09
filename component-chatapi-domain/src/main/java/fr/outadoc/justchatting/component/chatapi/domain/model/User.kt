package fr.outadoc.justchatting.component.chatapi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val login: String,
    val displayName: String,
    val description: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: String? = null,
) : Parcelable
