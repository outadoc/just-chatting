package fr.outadoc.justchatting.feature.emotes.data.recent

import fr.outadoc.justchatting.db.Recent_channels
import kotlinx.coroutines.flow.Flow

internal interface RecentChannelsDao {
    fun getAll(): Flow<List<Recent_channels>>
    fun insert(channel: Recent_channels)
}
