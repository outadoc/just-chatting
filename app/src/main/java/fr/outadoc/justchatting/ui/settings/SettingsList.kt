package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.repository.AppPreferences

@Preview
@Composable
fun SettingsListPreview() {
    Mdc3Theme {
        SettingsList(
            appPreferences = AppPreferences(),
            onAppPreferencesChange = {},
            onOpenNotificationPreferences = {},
            onOpenBubblePreferences = {}
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
    itemInsets: PaddingValues = PaddingValues()
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        SettingsHeader(
            modifier = Modifier
                .padding(itemInsets)
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.settings_chat))
        }

        SettingsSwitch(
            modifier = Modifier.padding(itemInsets),
            checked = appPreferences.animateEmotes,
            onCheckedChange = { checked ->
                onAppPreferencesChange(appPreferences.copy(animateEmotes = checked))
            }
        ) {
            Text(stringResource(R.string.animated_emotes))
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsSwitch(
            modifier = Modifier.padding(itemInsets),
            checked = appPreferences.showTimestamps,
            onCheckedChange = { checked ->
                onAppPreferencesChange(appPreferences.copy(showTimestamps = checked))
            }
        ) {
            Text(stringResource(R.string.timestamps))
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

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

        Divider(modifier = Modifier.padding(vertical = 4.dp))

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

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsHeader(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(itemInsets)
        ) {
            Text(stringResource(R.string.settings_notifications_header))
        }

        SettingsLink(
            modifier = Modifier.padding(itemInsets),
            onClick = onOpenNotificationPreferences,
            onClickLabel = stringResource(R.string.settings_notifications_openNotificationsSettings)
        ) {
            Text(text = stringResource(R.string.settings_notifications_openNotificationsSettings))
        }

        SettingsLink(
            modifier = Modifier.padding(itemInsets),
            onClick = onOpenBubblePreferences,
            onClickLabel = stringResource(R.string.settings_notifications_openBubbleSettings)
        ) {
            Text(text = stringResource(R.string.settings_notifications_openBubbleSettings))
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsHeader(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(itemInsets)
        ) {
            Text(stringResource(R.string.api_settings))
        }

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

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsHeader(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(itemInsets)
        ) {
            Text(stringResource(R.string.settings_about_header))
        }

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
