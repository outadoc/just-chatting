package com.github.andreyasadchy.xtra.model.chat

interface RemoteImage {
    fun getUrl(screenDensity: Float): String?
}
