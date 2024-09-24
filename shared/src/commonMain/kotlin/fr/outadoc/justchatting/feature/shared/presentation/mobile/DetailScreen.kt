package fr.outadoc.justchatting.feature.shared.presentation.mobile

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
internal sealed interface DetailScreen : Parcelable {

    @Serializable
    data class Chat(val id: String) : DetailScreen

    @Serializable
    data object About : DetailScreen

    @Serializable
    data object Appearance : DetailScreen

    @Serializable
    data object DependencyCredits : DetailScreen

    @Serializable
    data object Notifications : DetailScreen

    @Serializable
    data object ThirdParties : DetailScreen
}
