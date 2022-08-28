package com.github.andreyasadchy.xtra.ui.common

interface NavigationHandler {
    fun viewChannel(id: String?, login: String?, name: String?, channelLogo: String?)
    fun openSearch()
    fun openSettings()
}
