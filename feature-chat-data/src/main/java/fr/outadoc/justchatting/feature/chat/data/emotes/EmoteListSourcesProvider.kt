package fr.outadoc.justchatting.feature.chat.data.emotes

fun interface EmoteListSourcesProvider {
    fun getSources(): List<EmoteListSource>
}
