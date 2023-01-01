package fr.outadoc.justchatting.feature.chat.data.emotes

interface EmoteListSource {

    suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>
    ): List<EmoteSetItem>
}
