package fr.outadoc.justchatting.feature.preferences.presentation

import dev.nucleusframework.updater.NucleusUpdater
import dev.nucleusframework.updater.UpdateInfo
import dev.nucleusframework.updater.UpdateResult
import dev.nucleusframework.updater.provider.GitHubProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

internal class DesktopAppUpdateChecker : AppUpdateChecker {
    private val updater =
        NucleusUpdater {
            provider = GitHubProvider(owner = "outadoc", repo = "just-chatting")
            differentialDownload = false
        }

    override val isSupported: Boolean
        get() = updater.isUpdateSupported()

    private var pendingUpdate: UpdateInfo? = null

    private val _state = MutableStateFlow(AppUpdateState())
    override val state: StateFlow<AppUpdateState> = _state.asStateFlow()

    override suspend fun checkForUpdate() {
        pendingUpdate = null
        _state.update { AppUpdateState(isChecking = true) }

        when (val result = updater.checkForUpdates()) {
            is UpdateResult.Available -> {
                pendingUpdate = result.info
                _state.update { AppUpdateState(hasChecked = true, availableVersion = result.info.version) }
            }

            UpdateResult.NotAvailable -> {
                _state.update { AppUpdateState(hasChecked = true) }
            }

            is UpdateResult.Error -> {
                _state.update { AppUpdateState(hasChecked = true, error = result.exception.message) }
            }
        }
    }

    override suspend fun installUpdate() {
        val info = pendingUpdate ?: return

        _state.update { it.copy(isDownloading = true, downloadProgress = null, error = null) }

        try {
            var installer: File? = null
            updater.downloadUpdate(info).collect { progress ->
                installer = progress.file
                _state.update { it.copy(downloadProgress = (progress.percent / PERCENT_MAX).toFloat()) }
            }

            installer?.let(updater::installAndRestart)
        } catch (e: Exception) {
            _state.update { it.copy(isDownloading = false, error = e.message) }
        }
    }

    private companion object {
        const val PERCENT_MAX = 100.0
    }
}
