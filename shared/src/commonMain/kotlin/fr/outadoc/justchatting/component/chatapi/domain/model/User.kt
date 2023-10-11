package fr.outadoc.justchatting.component.chatapi.domain.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val login: String,
    val displayName: String,
    val description: String,
    val profileImageUrl: String? = null,
    val createdAt: String,
) : Parcelable
