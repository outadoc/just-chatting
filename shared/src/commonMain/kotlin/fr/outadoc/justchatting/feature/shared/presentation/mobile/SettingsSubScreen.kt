package fr.outadoc.justchatting.feature.shared.presentation.mobile

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
internal sealed interface SettingsSubScreen : Parcelable {

    @Serializable
    data object About : SettingsSubScreen

    @Serializable
    data object Appearance : SettingsSubScreen

    @Serializable
    data object DependencyCredits : SettingsSubScreen

    @Serializable
    data object Notifications : SettingsSubScreen

    @Serializable
    data object ThirdParties : SettingsSubScreen
}
