package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem

public fun interface EmoteListSourcesProvider {
    public fun getSources(): List<EmoteListSource<List<EmoteSetItem>>>
}
