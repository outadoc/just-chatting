package fr.outadoc.justchatting.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.composepreview.ThemePreviews
import fr.outadoc.justchatting.repository.AppPreferences
import fr.outadoc.justchatting.util.plus

@ThemePreviews
@Composable
fun SettingsListPreview() {
    MaterialTheme {
        SettingsList(
            appPreferences = AppPreferences(),
            onAppPreferencesChange = {},
            onOpenNotificationPreferences = {},
            onOpenBubblePreferences = {},
            onLogoutClick = {}
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
    onLogoutClick: () -> Unit,
    itemInsets: PaddingValues = PaddingValues(),
    insets: PaddingValues = PaddingValues()
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = insets + PaddingValues(bottom = 16.dp)
    ) {
        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(itemInsets)
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.settings_chat))
            }
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.animateEmotes,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(animateEmotes = checked))
                }
            ) {
                Text(stringResource(R.string.animated_emotes))
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.showTimestamps,
                onCheckedChange = { checked ->
                    onAppPreferencesChange(appPreferences.copy(showTimestamps = checked))
                }
            ) {
                Text(stringResource(R.string.timestamps))
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsSlider(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
                value = appPreferences.messageLimit,
                onValueChange = { value ->
                    onAppPreferencesChange(appPreferences.copy(messageLimit = value))
                },
                valueRange = AppPreferences.Defaults.ChatLimitRange,
                steps = 10,
                valueContent = { value ->
                    Text(
                        modifier = Modifier.width(48.dp),
                        text = "%,d".format(value)
                    )
                }
            ) {
                Text(stringResource(R.string.message_limit))
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsSlider(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
                value = appPreferences.recentMsgLimit,
                onValueChange = { value ->
                    onAppPreferencesChange(appPreferences.copy(recentMsgLimit = value))
                },
                valueRange = AppPreferences.Defaults.RecentChatLimitRange,
                steps = 10,
                valueContent = { value ->
                    if (value == 0) {
                        Text(text = stringResource(R.string.recentMsg_limit_disabled))
                    } else {
                        Text(
                            modifier = Modifier.width(48.dp),
                            text = "%,d".format(value)
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.recentMsg_limit))
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets)
            ) {
                Text(stringResource(R.string.settings_notifications_header))
            }
        }

        if (Build.VERSION.SDK_INT >= 26) {
            item {
                SettingsLink(
                    modifier = Modifier.padding(itemInsets),
                    onClick = onOpenNotificationPreferences,
                    onClickLabel = stringResource(R.string.settings_notifications_openNotificationsSettings)
                ) {
                    Text(text = stringResource(R.string.settings_notifications_openNotificationsSettings))
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 29) {
            item {
                SettingsLink(
                    modifier = Modifier.padding(itemInsets),
                    onClick = onOpenBubblePreferences,
                    onClickLabel = stringResource(R.string.settings_notifications_openBubbleSettings)
                ) {
                    Text(text = stringResource(R.string.settings_notifications_openBubbleSettings))
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets)
            ) {
                Text(stringResource(R.string.settings_account_header))
            }
        }

        item {
            var showLogoutDialog by remember { mutableStateOf(false) }
            SettingsLink(
                modifier = Modifier.padding(itemInsets),
                onClick = { showLogoutDialog = true },
                onClickLabel = null
            ) {
                Text(text = stringResource(R.string.settings_account_logout_action))
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = {
                        Text(text = stringResource(R.string.logout_title))
                    },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.logout_msg,
                                appPreferences.appUser.login ?: ""
                            )
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
                            }
                        ) {
                            Text(text = stringResource(R.string.yes))
                        }
                    }
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
                    .padding(itemInsets)
            ) {
                Text(stringResource(R.string.api_settings))
            }
        }

        item {
            SettingsEdit(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
                value = appPreferences.helixClientId,
                placeholder = {
                    Text(text = "aaaa-bbbb-cccc-dddd-eeee-ffff")
                },
                onValueChange = { value ->
                    onAppPreferencesChange(appPreferences.copy(helixClientId = value))
                }
            ) {
                Text(stringResource(R.string.api_helix))
            }
        }

        item {
            SettingsEdit(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets),
                value = appPreferences.helixRedirect,
                placeholder = {
                    Text(text = "https://example.com")
                },
                onValueChange = { value ->
                    onAppPreferencesChange(appPreferences.copy(helixRedirect = value))
                }
            ) {
                Text(stringResource(R.string.api_helix_redirect))
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsHeader(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(itemInsets)
            ) {
                Text(stringResource(R.string.settings_about_header))
            }
        }

        item {
            SettingsLink(
                modifier = Modifier.padding(itemInsets),
                onClick = {},
                onClickLabel = null
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_about_version,
                        stringResource(R.string.app_name),
                        BuildConfig.VERSION_NAME
                    )
                )
            }
        }
    }
}
