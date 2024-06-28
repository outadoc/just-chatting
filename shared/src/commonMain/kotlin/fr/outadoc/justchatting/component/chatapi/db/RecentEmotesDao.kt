package fr.outadoc.justchatting.component.chatapi.db

import kotlinx.coroutines.flow.Flow

interface RecentEmotesDao {
    fun getAll(): Flow<List<Recent_emotes>>
    fun insertAll(emotes: Collection<Recent_emotes>)
}
