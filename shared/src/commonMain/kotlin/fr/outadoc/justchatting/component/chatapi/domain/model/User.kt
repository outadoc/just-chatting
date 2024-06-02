package fr.outadoc.justchatting.component.chatapi.domain.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize

@Parcelize
data class User(
    val id: String,
    val login: String,
    val displayName: String,
    val description: String,
    val profileImageUrl: String? = null,
    val createdAt: String,
) : Parcelable
