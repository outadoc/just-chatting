package fr.outadoc.justchatting.feature.emotes.data.recent

import fr.outadoc.justchatting.db.Recent_emotes
import kotlinx.coroutines.flow.Flow

internal interface RecentEmotesDao {
    fun getAll(): Flow<List<Recent_emotes>>
    fun insertAll(emotes: Collection<Recent_emotes>)
}
