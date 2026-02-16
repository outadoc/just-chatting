package fr.outadoc.justchatting.feature.preferences.presentation

import platform.Foundation.NSBundle

internal class AppleAppVersionNameProvider : AppVersionNameProvider {
    override val appVersionName: String?
        get() =
            NSBundle.Companion.mainBundle
                .objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
}
