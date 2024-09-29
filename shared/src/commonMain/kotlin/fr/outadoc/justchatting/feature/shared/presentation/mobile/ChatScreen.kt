package fr.outadoc.justchatting.feature.shared.presentation.mobile

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
internal data class ChatScreen(val id: String) : Parcelable
