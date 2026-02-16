package fr.outadoc.justchatting.feature.preferences.presentation.ui

import fr.outadoc.justchatting.feature.preferences.presentation.AppVersionNameProvider
import platform.Foundation.NSBundle

internal class AppleAppVersionNameProvider : AppVersionNameProvider {
    override val appVersionName: String?
        get() =
            NSBundle.mainBundle
                .objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
}
