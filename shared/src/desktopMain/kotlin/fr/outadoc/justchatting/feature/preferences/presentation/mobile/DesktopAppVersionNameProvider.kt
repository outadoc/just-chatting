package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import fr.outadoc.justchatting.AppInfo

internal class DesktopAppVersionNameProvider : AppVersionNameProvider {
    override val appVersionName: String get() = AppInfo.APP_VERSION
}
