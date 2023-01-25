package fr.outadoc.justchatting.component.twitch.model

data class FfzEmote(
    val id: String,
    val code: String,
    val images: Map<String, String?>,
    val imageType: String,
    val animated: Boolean
)
