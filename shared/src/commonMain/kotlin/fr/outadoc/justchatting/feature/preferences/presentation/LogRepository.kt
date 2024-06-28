package fr.outadoc.justchatting.feature.preferences.presentation

internal interface LogRepository {
    val isSupported: Boolean
    suspend fun dumpLogs(): String
}
