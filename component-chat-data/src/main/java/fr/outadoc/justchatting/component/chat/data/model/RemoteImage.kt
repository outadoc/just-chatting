package fr.outadoc.justchatting.component.chat.data.model

interface RemoteImage {
    fun getUrl(screenDensity: Float): String?
}
