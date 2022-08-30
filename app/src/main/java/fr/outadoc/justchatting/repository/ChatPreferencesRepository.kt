package fr.outadoc.justchatting.repository

import kotlinx.coroutines.flow.Flow

interface ChatPreferencesRepository {
    val animateEmotes: Flow<Boolean>
    val showTimestamps: Flow<Boolean>
    val enableRecentMsg: Flow<Boolean>
    val recentMsgLimit: Flow<Int>
    val messageLimit: Flow<Int>
}
