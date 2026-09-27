package fr.outadoc.justchatting.feature.shared.presentation

import fr.outadoc.justchatting.utils.presentation.toNSURL
import platform.Foundation.NSURL

/** The Twitch authorization page to open, for Swift. */
public val MainRouterViewModel.Event.ShowAuthPage.url: NSURL?
    get() = uri.toNSURL()
