package fr.outadoc.justchatting.component.twitch.model

interface RemoteImage {
    fun getUrl(screenDensity: Float): String?
}
