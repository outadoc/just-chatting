package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.recent.domain.model.RecentChannel
import kotlinx.coroutines.flow.Flow

internal interface RecentChannelsApi {
    fun getAll(): Flow<List<RecentChannel>>
    fun insert(channel: RecentChannel)
}
