package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem

internal fun interface EmoteListSourcesProvider {
    fun getSources(): List<EmoteListSource<List<EmoteSetItem>>>
}
