package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import kotlinx.coroutines.flow.Flow

internal interface RecentEmotesApi {
    public fun getAll(): Flow<List<RecentEmote>>

    public fun insertAll(emotes: Collection<RecentEmote>)
}
