package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.utils.presentation.toNSURL
import platform.Foundation.NSURL

/** The exported log file to share, for Swift. */
public val SettingsViewModel.Event.ShareLogs.url: NSURL?
    get() = uri.toNSURL()
