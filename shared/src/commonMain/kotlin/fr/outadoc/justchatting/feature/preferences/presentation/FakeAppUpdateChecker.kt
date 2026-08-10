package fr.outadoc.justchatting.feature.preferences.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Simulates an update being available, without touching any real update mechanism.
 * Useful to manually exercise update-related UI without waiting for a real release.
 */
internal class FakeAppUpdateChecker : AppUpdateChecker {
    override val isSupported: Boolean = true

    private val _state = MutableStateFlow(AppUpdateState())
    override val state: StateFlow<AppUpdateState> = _state.asStateFlow()

    override suspend fun checkForUpdate() {
        _state.update { AppUpdateState(isChecking = true) }
        delay(1.5.seconds)
        _state.update {
            AppUpdateState(
                hasChecked = true,
                availableVersion = "99.9.9",
            )
        }
    }

    override suspend fun installUpdate() {
        _state.update { it.copy(isDownloading = true, downloadProgress = 0f, error = null) }

        var progress = 0f
        while (progress < 1f) {
            delay(200.milliseconds)
            progress = (progress + 0.1f).coerceAtMost(1f)
            _state.update { it.copy(downloadProgress = progress) }
        }

        // A real implementation would install the update and restart the app here.
        _state.update { it.copy(isDownloading = false) }
    }
}
