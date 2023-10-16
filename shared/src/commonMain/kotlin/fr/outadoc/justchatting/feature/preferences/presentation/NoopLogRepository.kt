package fr.outadoc.justchatting.feature.preferences.presentation

class NoopLogRepository : LogRepository {

    override val isSupported: Boolean = false

    override suspend fun dumpLogs(): String = ""
}
