package fr.outadoc.justchatting.component.chatapi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val login: String,
    val displayName: String,
    val description: String,
    val profileImageUrl: String? = null,
    val createdAt: String,
) : Parcelable
