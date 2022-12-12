package fr.outadoc.justchatting.feature.chat.data.model

interface RemoteImage {
    fun getUrl(screenDensity: Float): String?
}
