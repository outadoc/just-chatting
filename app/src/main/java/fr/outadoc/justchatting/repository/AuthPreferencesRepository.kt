package fr.outadoc.justchatting.repository

import kotlinx.coroutines.flow.Flow

interface AuthPreferencesRepository {
    val helixClientId: Flow<String>
    val helixRedirect: Flow<String>
}
