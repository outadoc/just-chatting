package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.recent.domain.model.RecentEmote
import kotlinx.coroutines.flow.Flow

internal interface RecentEmotesApi {
    fun getAll(): Flow<List<RecentEmote>>
    fun insertAll(emotes: Collection<RecentEmote>)
}
