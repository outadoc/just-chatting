package fr.outadoc.justchatting.feature.preferences.presentation

import kotlinx.coroutines.flow.StateFlow

public interface AppUpdateChecker {
    public val isSupported: Boolean

    public val state: StateFlow<AppUpdateState>

    public suspend fun checkForUpdate()

    public suspend fun installUpdate()
}

public data class AppUpdateState(
    val hasChecked: Boolean = false,
    val isChecking: Boolean = false,
    val availableVersion: String? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Float? = null,
    val error: String? = null,
)
