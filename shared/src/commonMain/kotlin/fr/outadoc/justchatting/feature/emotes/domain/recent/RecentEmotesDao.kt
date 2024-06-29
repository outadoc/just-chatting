package fr.outadoc.justchatting.feature.emotes.domain.recent

import fr.outadoc.justchatting.data.db.Recent_emotes
import kotlinx.coroutines.flow.Flow

internal interface RecentEmotesDao {
    fun getAll(): Flow<List<Recent_emotes>>
    fun insertAll(emotes: Collection<Recent_emotes>)
}