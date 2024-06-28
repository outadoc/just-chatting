package fr.outadoc.justchatting.feature.chat.data.emotes

internal interface EmoteListSource<T> {

    suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): Result<T>
}
