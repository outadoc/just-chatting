package fr.outadoc.justchatting.component.chatapi.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class DbRecentEmotesRepository(
    private val recentEmoteQueries: RecentEmoteQueries,
) : RecentEmotesRepository {

    override fun getAll(): Flow<List<Recent_emotes>> {
        return recentEmoteQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override fun insertAll(emotes: Collection<Recent_emotes>) {
        recentEmoteQueries.transaction {
            emotes.forEach { emote ->
                recentEmoteQueries.insert(
                    name = emote.name,
                    url = emote.url,
                    used_at = emote.used_at,
                )
            }

            recentEmoteQueries.cleanUp(limit = MaxRecentEmotes)
        }
    }
}
