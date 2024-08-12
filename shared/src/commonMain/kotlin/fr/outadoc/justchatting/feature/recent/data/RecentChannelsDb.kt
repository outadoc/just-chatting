package fr.outadoc.justchatting.feature.recent.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.UserQueries
import fr.outadoc.justchatting.feature.recent.domain.RecentChannelsApi
import fr.outadoc.justchatting.feature.recent.domain.model.RecentChannel
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

internal class RecentChannelsDb(
    private val recentChannelQueries: UserQueries,
) : RecentChannelsApi {

    override fun getAll(): Flow<List<RecentChannel>> {
        return recentChannelQueries
            .getAll()
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { channels ->
                channels.map { channel ->
                    RecentChannel(
                        id = channel.id,
                        usedAt = Instant.fromEpochMilliseconds(channel.used_at),
                    )
                }
            }
    }

    override fun insert(channel: RecentChannel) {
        recentChannelQueries.transaction {
            recentChannelQueries.insert(
                id = channel.id,
                used_at = channel.usedAt.toEpochMilliseconds(),
            )
        }
    }
}
