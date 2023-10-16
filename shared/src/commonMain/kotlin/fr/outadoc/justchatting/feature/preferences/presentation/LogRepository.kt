package fr.outadoc.justchatting.feature.preferences.presentation

interface LogRepository {
    val isSupported: Boolean
    suspend fun dumpLogs(): String
}
