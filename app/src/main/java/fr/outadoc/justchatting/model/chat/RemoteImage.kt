package fr.outadoc.justchatting.model.chat

interface RemoteImage {
    fun getUrl(screenDensity: Float): String?
}
