package fr.outadoc.justchatting.feature.emotes.domain

internal interface EmoteListSource<T> {
    suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): Result<T>
}
