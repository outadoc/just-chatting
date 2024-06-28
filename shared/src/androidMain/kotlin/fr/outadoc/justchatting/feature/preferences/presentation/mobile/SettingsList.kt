package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.plus

@ThemePreviews
@Composable
internal fun SettingsListPreview() {
    AppTheme {
        SettingsList(
            appPreferences = AppPreferences(),
            onAppPreferencesChange = {},
            onOpenNotificationPreferences = {},
            onOpenBubblePreferences = {},
            onOpenAccessibilityPreferences = {},
            onLogoutClick = {},
            onShareLogsClick = {},
            onOpenDependencyCredits = {},
            versionName = "1.2.3",
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun SettingsList(
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences,
    onAppPreferencesChange: (AppPreferences) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
    onLogoutClick: () -> Unit,
    onShareLogsClick: () -> Unit,
    onOpenDependencyCredits: () -> Unit,
    itemInsets: PaddingValues = PaddingValues(horizontal = 16.dp),
    insets: PaddingValues = PaddingValues(),
    versionName: String,
) {
    val uriHandler = LocalUriHandler.current

    val appUser = appPreferences.appUser

    LazyColumn(
        modifier = modifier,
        contentPadding = insets + PaddingValues(bottom = 16.dp),
    ) {
        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(MR.strings.settings_thirdparty_recent_header))
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
                    Text(stringResource(MR.strings.settings_thirdparty_recent_title))
                },
                subtitle = {
                    Text(stringResource(MR.strings.settings_thirdparty_recent_subtitle))
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(MR.strings.settings_thirdparty_pronouns_header))
            }
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enablePronouns,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enablePronouns = checked))
                },
                title = {
                    Text(stringResource(MR.strings.settings_thirdparty_pronouns_title))
                },
                subtitle = {
                    Text(stringResource(MR.strings.settings_thirdparty_pronouns_subtitle))
                },
            )
        }

        item {
            val pronounsUrl = stringResource(MR.strings.app_pronouns_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(pronounsUrl) },
                onClickLabel = stringResource(MR.strings.settings_thirdparty_pronouns_set_cd),
                title = { Text(text = stringResource(MR.strings.settings_thirdparty_pronouns_set_title)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(MR.strings.settings_thirdparty_emotes_header))
            }
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enableBttvEmotes,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enableBttvEmotes = checked))
                },
                title = {
                    Text(stringResource(MR.strings.settings_thirdparty_bttv_title))
                },
                subtitle = {
                    Text(stringResource(MR.strings.settings_thirdparty_bttv_subtitle))
                },
            )
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enableFfzEmotes,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enableFfzEmotes = checked))
                },
                title = {
                    Text(stringResource(MR.strings.settings_thirdparty_ffz_title))
                },
                subtitle = {
                    Text(stringResource(MR.strings.settings_thirdparty_ffz_subtitle))
                },
            )
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.enableStvEmotes,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(enableStvEmotes = checked))
                },
                title = {
                    Text(stringResource(MR.strings.settings_thirdparty_stv_title))
                },
                subtitle = {
                    Text(stringResource(MR.strings.settings_thirdparty_stv_subtitle))
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(MR.strings.settings_notifications_header))
            }
        }

        item {
            val notificationPermissionState: PermissionState =
                rememberPermissionState("android.permission.POST_NOTIFICATIONS")

            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(MR.strings.settings_notifications_enable_title)) },
                subtitle = { Text(text = stringResource(MR.strings.settings_notifications_enable_subtitle)) },
                checked = notificationPermissionState.status.isGranted && appPreferences.enableNotifications,
                onCheckedChange = { checked ->
                    if (checked) {
                        notificationPermissionState.launchPermissionRequest()
                    }

                    onAppPreferencesChange(
                        appPreferences.copy(enableNotifications = checked),
                    )
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = onOpenNotificationPreferences,
                onClickLabel = stringResource(MR.strings.settings_notifications_openNotificationsSettings),
                title = {
                    Text(text = stringResource(MR.strings.settings_notifications_openNotificationsSettings))
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }

        if (Build.VERSION.SDK_INT >= 29) {
            item {
                SettingsText(
                    modifier = Modifier.padding(itemInsets),
                    onClick = onOpenBubblePreferences,
                    onClickLabel = stringResource(MR.strings.settings_notifications_openBubbleSettings),
                    title = {
                        Text(text = stringResource(MR.strings.settings_notifications_openBubbleSettings))
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                        )
                    },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(itemInsets)
                    .padding(top = 8.dp),
            ) {
                Text(stringResource(MR.strings.settings_accessibility_header))
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
                    Text(stringResource(MR.strings.settings_accessibility_timestamps_title))
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = onOpenAccessibilityPreferences,
                onClickLabel = stringResource(MR.strings.settings_accessibility_animations_action),
                title = {
                    Text(stringResource(MR.strings.settings_accessibility_animations_title))
                },
                subtitle = {
                    Text(stringResource(MR.strings.settings_accessibility_animations_subtitle))
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(MR.strings.settings_about_header))
            }
        }

        item {
            val repoUrl = stringResource(MR.strings.app_repo_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(repoUrl) },
                onClickLabel = stringResource(MR.strings.settings_about_repo_cd),
                title = { Text(text = stringResource(MR.strings.settings_about_repo_title)) },
                subtitle = { Text(text = stringResource(MR.strings.app_repo_name)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                    )
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(MR.strings.settings_logs_title)) },
                onClick = onShareLogsClick,
                subtitle = { Text(text = stringResource(MR.strings.settings_logs_subtitle)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                    )
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(MR.strings.settings_dependencies_header)) },
                onClick = onOpenDependencyCredits,
            )
        }

        item {
            val licenseUrl = stringResource(MR.strings.app_license_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(licenseUrl) },
                onClickLabel = stringResource(MR.strings.settings_about_license_cd),
                title = { Text(text = stringResource(MR.strings.settings_about_license_title)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            MR.strings.settings_about_license_subtitle,
                            stringResource(MR.strings.app_name),
                            stringResource(MR.strings.app_license_name),
                        ),
                    )
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(MR.strings.app_name)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            MR.strings.settings_about_version,
                            versionName,
                        ),
                    )
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
            ) {
                Text(stringResource(MR.strings.settings_account_header))
            }
        }

        if (appUser is AppUser.LoggedIn) {
            item {
                var showLogoutDialog by remember { mutableStateOf(false) }
                SettingsText(
                    modifier = Modifier.padding(itemInsets),
                    onClick = { showLogoutDialog = true },
                    onClickLabel = null,
                    title = {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.error,
                        ) {
                            Text(text = stringResource(MR.strings.settings_account_logout_action))
                        }
                    },
                )

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text(text = stringResource(MR.strings.logout_title)) },
                        text = {
                            Text(
                                text = stringResource(
                                    MR.strings.logout_msg,
                                    appUser.userLogin,
                                ),
                            )
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text(text = stringResource(MR.strings.no))
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onLogoutClick()
                                    showLogoutDialog = false
                                },
                            ) {
                                Text(text = stringResource(MR.strings.yes))
                            }
                        },
                    )
                }
            }
        }
    }
}
