package fr.outadoc.justchatting.feature.chat.data.emotes

internal fun interface EmoteListSourcesProvider {
    fun getSources(): List<EmoteListSource<List<EmoteSetItem>>>
}
