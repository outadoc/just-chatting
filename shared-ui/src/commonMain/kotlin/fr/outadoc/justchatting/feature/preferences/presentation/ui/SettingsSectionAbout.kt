package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.preferences.presentation.AppUpdateState
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.all_goBack
import fr.outadoc.justchatting.shared.internal.app_license_name
import fr.outadoc.justchatting.shared.internal.app_license_url
import fr.outadoc.justchatting.shared.internal.app_name
import fr.outadoc.justchatting.shared.internal.app_repo_name
import fr.outadoc.justchatting.shared.internal.app_repo_url
import fr.outadoc.justchatting.shared.internal.settings_about_header
import fr.outadoc.justchatting.shared.internal.settings_about_license_cd
import fr.outadoc.justchatting.shared.internal.settings_about_license_subtitle
import fr.outadoc.justchatting.shared.internal.settings_about_license_title
import fr.outadoc.justchatting.shared.internal.settings_about_repo_cd
import fr.outadoc.justchatting.shared.internal.settings_about_repo_title
import fr.outadoc.justchatting.shared.internal.settings_about_update_cd
import fr.outadoc.justchatting.shared.internal.settings_about_update_subtitle_available
import fr.outadoc.justchatting.shared.internal.settings_about_update_subtitle_checking
import fr.outadoc.justchatting.shared.internal.settings_about_update_subtitle_error
import fr.outadoc.justchatting.shared.internal.settings_about_update_subtitle_never
import fr.outadoc.justchatting.shared.internal.settings_about_update_subtitle_upToDate
import fr.outadoc.justchatting.shared.internal.settings_about_update_title
import fr.outadoc.justchatting.shared.internal.settings_about_version
import fr.outadoc.justchatting.shared.internal.settings_about_xtra_subtitle
import fr.outadoc.justchatting.shared.internal.settings_about_xtra_title
import fr.outadoc.justchatting.shared.internal.settings_logs_copiedToClipboard
import fr.outadoc.justchatting.shared.internal.settings_logs_subtitle
import fr.outadoc.justchatting.shared.internal.settings_logs_title
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionAbout(
    modifier: Modifier = Modifier,
    canNavigateUp: Boolean = true,
    onNavigateUp: () -> Unit = {},
    onShareLogs: (Uri) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_about_header)) },
                navigationIcon = {
                    if (canNavigateUp) {
                        AccessibleIconButton(
                            onClick = onNavigateUp,
                            onClickLabel = stringResource(Res.string.all_goBack),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { insets ->
        SettingsSectionAboutContent(
            modifier = modifier,
            insets = insets,
            itemInsets = PaddingValues(horizontal = 16.dp),
            snackbarHostState = snackbarHostState,
            onShareLogs = onShareLogs,
        )
    }
}

@Composable
private fun SettingsSectionAboutContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    itemInsets: PaddingValues = SettingsConstants.ItemInsets,
    snackbarHostState: SnackbarHostState,
    onShareLogs: (Uri) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val viewModel: SettingsViewModel = koinViewModel()

    val state by viewModel.state.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val logsCopiedMessage = stringResource(Res.string.settings_logs_copiedToClipboard)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.Event.ShareLogs -> {
                    onShareLogs(event.uri)
                }

                is SettingsViewModel.Event.CopyLogsToClipboard -> {
                    clipboard.setText(AnnotatedString(event.text))
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = logsCopiedMessage,
                            duration = SnackbarDuration.Short,
                        )
                    }
                }

                is SettingsViewModel.Event.NavigateToDetail -> Unit
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = insets,
    ) {
        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(Res.string.app_name)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            Res.string.settings_about_version,
                            state.appVersionName.orEmpty(),
                        ),
                    )
                },
            )
        }

        if (state.isUpdateCheckSupported) {
            item {
                val updateState = state.updateState

                SettingsText(
                    modifier = Modifier.padding(itemInsets),
                    onClick = {
                        if (updateState.availableVersion != null) {
                            showUpdateDialog = true
                        } else {
                            viewModel.checkForUpdates()
                        }
                    },
                    onClickLabel = stringResource(Res.string.settings_about_update_cd),
                    title = { Text(text = stringResource(Res.string.settings_about_update_title)) },
                    subtitle = {
                        Text(text = updateState.subtitle())
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                        )
                    },
                )

                if (showUpdateDialog) {
                    UpdateAvailableDialog(
                        state = updateState,
                        onConfirm = viewModel::installUpdate,
                        onDismiss = { showUpdateDialog = false },
                    )
                }
            }
        }

        item {
            val licenseUrl = stringResource(Res.string.app_license_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(licenseUrl) },
                onClickLabel = stringResource(Res.string.settings_about_license_cd),
                title = { Text(text = stringResource(Res.string.settings_about_license_title)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            Res.string.settings_about_license_subtitle,
                            stringResource(Res.string.app_name),
                            stringResource(Res.string.app_license_name),
                        ),
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri("https://github.com/crackededed/Xtra") },
                onClickLabel = stringResource(Res.string.settings_about_license_cd),
                title = { Text(text = stringResource(Res.string.settings_about_xtra_title)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            Res.string.settings_about_xtra_subtitle,
                            stringResource(Res.string.app_name),
                        ),
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }

        item {
            val repoUrl = stringResource(Res.string.app_repo_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(repoUrl) },
                onClickLabel = stringResource(Res.string.settings_about_repo_cd),
                title = { Text(text = stringResource(Res.string.settings_about_repo_title)) },
                subtitle = { Text(text = stringResource(Res.string.app_repo_name)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(Res.string.settings_logs_title)) },
                onClick = viewModel::onExportLogsClick,
                subtitle = { Text(text = stringResource(Res.string.settings_logs_subtitle)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun AppUpdateState.subtitle(): String = when {
    isChecking -> stringResource(Res.string.settings_about_update_subtitle_checking)

    error != null -> stringResource(Res.string.settings_about_update_subtitle_error)

    availableVersion != null -> {
        stringResource(Res.string.settings_about_update_subtitle_available, availableVersion.orEmpty())
    }

    hasChecked -> stringResource(Res.string.settings_about_update_subtitle_upToDate)

    else -> stringResource(Res.string.settings_about_update_subtitle_never)
}
