package fr.outadoc.justchatting.repository

interface PreferenceRepository :
    AuthPreferencesRepository,
    ChatPreferencesRepository,
    UserPreferencesRepository
