package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.feature.preferences.presentation.Dependency
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.plus

@ThemePreviews
@Composable
fun SettingsListPreview() {
    AppTheme {
        SettingsList(
            appPreferences = AppPreferences(),
            onAppPreferencesChange = {},
            onOpenNotificationPreferences = {},
            onOpenBubblePreferences = {},
            onOpenAccessibilityPreferences = {},
            onLogoutClick = {},
            onShareLogsClick = {},
            readDependencies = { emptyList() },
            versionName = "1.2.3",
        )
    }
}

@Composable
fun SettingsList(
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences,
    onAppPreferencesChange: (AppPreferences) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
    onLogoutClick: () -> Unit,
    onShareLogsClick: () -> Unit,
    itemInsets: PaddingValues = PaddingValues(),
    insets: PaddingValues = PaddingValues(),
    readDependencies: ReadExternalDependenciesList,
    versionName: String,
) {
    val uriHandler = LocalUriHandler.current
    var deps: List<Dependency> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(readDependencies) {
        deps = readDependencies()
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = insets + PaddingValues(bottom = 16.dp),
    ) {
        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(itemInsets)
                    .padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.settings_accessibility_header))
            }
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.showTimestamps,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(showTimestamps = checked))
                },
                title = {
                    Text(stringResource(R.string.settings_accessibility_timestamps_title))
                },
                subtitle = {
                    Text(stringResource(R.string.settings_accessibility_timestamps_subtitle))
                },
            )
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = onOpenAccessibilityPreferences,
                onClickLabel = stringResource(R.string.settings_accessibility_animations_action),
                title = {
                    Text(stringResource(R.string.settings_accessibility_animations_title))
                },
                subtitle = {
                    Text(stringResource(R.string.settings_accessibility_animations_subtitle))
                },
            )
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(R.string.settings_thirdparty_header))
            }
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enableRecentMessages,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enableRecentMessages = checked))
                },
                title = {
                    Text(stringResource(R.string.settings_thirdparty_recent_title))
                },
                subtitle = {
                    Text(stringResource(R.string.settings_thirdparty_recent_subtitle))
                },
            )
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enableBttvEmotes,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enableBttvEmotes = checked))
                },
                title = {
                    Text(stringResource(R.string.settings_thirdparty_bttv_title))
                },
                subtitle = {
                    Text(stringResource(R.string.settings_thirdparty_bttv_subtitle))
                },
            )
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enableFfzEmotes,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enableFfzEmotes = checked))
                },
                title = {
                    Text(stringResource(R.string.settings_thirdparty_ffz_title))
                },
                subtitle = {
                    Text(stringResource(R.string.settings_thirdparty_ffz_subtitle))
                },
            )
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enableStvEmotes,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enableStvEmotes = checked))
                },
                title = {
                    Text(stringResource(R.string.settings_thirdparty_stv_title))
                },
                subtitle = {
                    Text(stringResource(R.string.settings_thirdparty_stv_subtitle))
                },
            )
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(R.string.settings_notifications_header))
            }
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = onOpenNotificationPreferences,
                onClickLabel = stringResource(R.string.settings_notifications_openNotificationsSettings),
                title = {
                    Text(text = stringResource(R.string.settings_notifications_openNotificationsSettings))
                },
            )
        }

        if (Build.VERSION.SDK_INT >= 29) {
            item {
                SettingsText(
                    modifier = Modifier.padding(itemInsets),
                    onClick = onOpenBubblePreferences,
                    onClickLabel = stringResource(R.string.settings_notifications_openBubbleSettings),
                    title = {
                        Text(text = stringResource(R.string.settings_notifications_openBubbleSettings))
                    },
                )
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(R.string.settings_account_header))
            }
        }

        item {
            var showLogoutDialog by remember { mutableStateOf(false) }
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { showLogoutDialog = true },
                onClickLabel = null,
                title = {
                    Text(text = stringResource(R.string.settings_account_logout_action))
                },
            )

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text(text = stringResource(R.string.logout_title)) },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.logout_msg,
                                appPreferences.appUser.login ?: "",
                            ),
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text(text = stringResource(R.string.no))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onLogoutClick()
                                showLogoutDialog = false
                            },
                        ) {
                            Text(text = stringResource(R.string.yes))
                        }
                    },
                )
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(R.string.settings_about_header))
            }
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(R.string.app_name)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            R.string.settings_about_version,
                            versionName,
                        ),
                    )
                },
            )
        }

        item {
            val repoUrl = stringResource(id = R.string.app_repo_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(repoUrl) },
                onClickLabel = stringResource(id = R.string.settings_about_repo_cd),
                title = { Text(text = stringResource(id = R.string.settings_about_repo_title)) },
                subtitle = { Text(text = stringResource(id = R.string.app_repo_name)) },
            )
        }

        item {
            val licenseUrl = stringResource(id = R.string.app_license_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(licenseUrl) },
                onClickLabel = stringResource(id = R.string.settings_about_license_cd),
                title = { Text(text = stringResource(id = R.string.settings_about_license_title)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            id = R.string.settings_about_license_subtitle,
                            stringResource(id = R.string.app_name),
                            stringResource(id = R.string.app_license_name),
                        ),
                    )
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(R.string.settings_logs_title)) },
                onClick = onShareLogsClick,
                subtitle = { Text(text = stringResource(R.string.settings_logs_subtitle)) },
            )
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(id = R.string.settings_dependencies_header))
            }
        }

        items(deps) { dependency ->
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = {
                    dependency.moduleUrl?.let { url ->
                        uriHandler.openUri(url)
                    }
                },
                onClickLabel = stringResource(id = R.string.settings_dependencies_cd)
                    .takeIf { dependency.moduleUrl != null },
                title = { Text(text = dependency.moduleName) },
                subtitle = {
                    dependency.moduleLicense?.let { license ->
                        Text(text = license)
                    }
                },
            )
        }
    }
}
