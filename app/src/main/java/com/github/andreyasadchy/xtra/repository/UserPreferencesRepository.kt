package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.model.User
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val user: Flow<User>
    suspend fun updateUser(user: User?)
}
