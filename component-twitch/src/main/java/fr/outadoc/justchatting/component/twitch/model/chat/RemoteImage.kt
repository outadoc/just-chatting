package fr.outadoc.justchatting.component.twitch.parser.model

interface RemoteImage {
    fun getUrl(screenDensity: Float): String?
}
