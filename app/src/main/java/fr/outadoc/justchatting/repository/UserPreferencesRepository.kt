package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.model.User
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val user: Flow<User>
    suspend fun updateUser(user: User?)
}
