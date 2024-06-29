package fr.outadoc.justchatting.feature.emotes.data.recent

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.component.chatapi.db.RecentChannelQueries
import fr.outadoc.justchatting.component.chatapi.db.Recent_channels
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow

internal class DbRecentChannelsDao(
    private val recentChannelQueries: RecentChannelQueries,
) : RecentChannelsDao {

    override fun getAll(): Flow<List<Recent_channels>> {
        return recentChannelQueries
            .getAll()
            .asFlow()
            .mapToList(DispatchersProvider.io)
    }

    override fun insert(channel: Recent_channels) {
        recentChannelQueries.transaction {
            recentChannelQueries.insert(
                id = channel.id,
                used_at = channel.used_at,
            )
            recentChannelQueries.cleanUp(limit = MAX_RECENT_CHANNELS)
        }
    }

    private companion object {
        const val MAX_RECENT_CHANNELS: Long = 50
    }
}
