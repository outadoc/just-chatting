package com.github.andreyasadchy.xtra.repository

import kotlinx.coroutines.flow.Flow

interface AuthPreferencesRepository {
    val helixClientId: Flow<String>
    val helixRedirect: Flow<String>
}
