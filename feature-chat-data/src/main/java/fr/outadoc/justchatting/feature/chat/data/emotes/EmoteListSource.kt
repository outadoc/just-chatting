package fr.outadoc.justchatting.feature.chat.data.emotes

interface EmoteListSource<T> {

    suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>
    ): T
}
