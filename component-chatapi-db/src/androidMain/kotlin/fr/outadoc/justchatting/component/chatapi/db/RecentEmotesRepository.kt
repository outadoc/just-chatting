package fr.outadoc.justchatting.component.chatapi.db

import kotlinx.coroutines.flow.Flow

interface RecentEmotesRepository {
    fun getAll(): Flow<List<Recent_emotes>>
    fun insertAll(emotes: Collection<Recent_emotes>)
}
