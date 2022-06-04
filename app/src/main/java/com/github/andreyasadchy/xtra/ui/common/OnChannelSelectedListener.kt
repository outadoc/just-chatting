package com.github.andreyasadchy.xtra.ui.common

interface OnChannelSelectedListener {
    fun viewChannel(
        id: String?,
        login: String?,
        name: String?,
        channelLogo: String?,
        updateLocal: Boolean = false
    )
}
