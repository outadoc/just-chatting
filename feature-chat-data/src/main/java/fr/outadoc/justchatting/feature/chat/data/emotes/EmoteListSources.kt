package fr.outadoc.justchatting.feature.chat.data.emotes

fun interface EmoteListSources {
    fun getSources(): List<EmoteListSource>
}
