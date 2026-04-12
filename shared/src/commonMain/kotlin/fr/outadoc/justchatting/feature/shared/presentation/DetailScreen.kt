package fr.outadoc.justchatting.feature.shared.presentation

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
public sealed interface DetailScreen : Parcelable {
    @Serializable
    public data class Chat(
        val id: String,
    ) : DetailScreen

    @Serializable
    public data object About : DetailScreen

    @Serializable
    public data object Appearance : DetailScreen

    @Serializable
    public data object DependencyCredits : DetailScreen

    @Serializable
    public data object Notifications : DetailScreen

    @Serializable
    public data object ThirdParties : DetailScreen
}
