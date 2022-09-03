package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.model.AppUser
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val appUser: Flow<AppUser>
    suspend fun updateUser(appUser: AppUser?)
}
