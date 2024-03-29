package fr.outadoc.justchatting.component.chatapi.db

import kotlinx.coroutines.flow.Flow

interface RecentChannelsRepository {
    fun getAll(): Flow<List<Recent_channels>>
    fun insert(channel: Recent_channels)
}
