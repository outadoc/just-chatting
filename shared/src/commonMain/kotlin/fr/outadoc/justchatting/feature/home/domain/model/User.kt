package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize

@Immutable
@Parcelize
internal data class User(
    val id: String,
    val login: String,
    val displayName: String,
    val description: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: String? = null,
) : Parcelable
