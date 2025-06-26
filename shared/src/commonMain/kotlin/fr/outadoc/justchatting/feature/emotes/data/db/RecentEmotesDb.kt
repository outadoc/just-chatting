package fr.outadoc.justchatting.feature.emotes.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.RecentEmoteQueries
import fr.outadoc.justchatting.feature.emotes.domain.RecentEmotesApi
import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

internal class RecentEmotesDb(
    private val recentEmoteQueries: RecentEmoteQueries,
) : RecentEmotesApi {

    override fun getAll(): Flow<List<RecentEmote>> {
        return recentEmoteQueries
            .getAll()
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { emotes ->
                emotes.map { emote ->
                    RecentEmote(
                        name = emote.name,
                        url = emote.url,
                        usedAt = Instant.fromEpochMilliseconds(emote.used_at),
                    )
                }
            }
    }

    override fun insertAll(emotes: Collection<RecentEmote>) {
        recentEmoteQueries.transaction {
            emotes.forEach { emote ->
                recentEmoteQueries.insert(
                    name = emote.name,
                    url = emote.url,
                    used_at = emote.usedAt.toEpochMilliseconds(),
                )
            }

            recentEmoteQueries.cleanUp(limit = MAX_RECENT_EMOTES)
        }
    }

    private companion object {
        const val MAX_RECENT_EMOTES: Long = 50
    }
}
