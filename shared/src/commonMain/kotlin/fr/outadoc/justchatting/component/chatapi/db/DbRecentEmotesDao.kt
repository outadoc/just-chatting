package fr.outadoc.justchatting.component.chatapi.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow

class DbRecentEmotesDao(
    private val recentEmoteQueries: RecentEmoteQueries,
) : RecentEmotesDao {

    override fun getAll(): Flow<List<Recent_emotes>> {
        return recentEmoteQueries
            .getAll()
            .asFlow()
            .mapToList(DispatchersProvider.io)
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

            recentEmoteQueries.cleanUp(limit = MAX_RECENT_EMOTES)
        }
    }

    private companion object {
        const val MAX_RECENT_EMOTES: Long = 50
    }
}
