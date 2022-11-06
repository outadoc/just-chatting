package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.layout.Column
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
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.repository.AppPreferences

@Preview
@Composable
fun SettingsListPreview() {
    Mdc3Theme {
        SettingsList(
            appPreferences = AppPreferences(),
            onAppPreferencesChange = {}
        )
    }
}

@Composable
fun SettingsList(
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences,
    onAppPreferencesChange: (AppPreferences) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.verticalScroll(scrollState)) {
        SettingsHeader(modifier = Modifier.padding(8.dp)) {
            Text(stringResource(R.string.settings_chat))
        }

        SettingsSwitch(
            modifier = Modifier.padding(horizontal = 8.dp),
            checked = appPreferences.animateEmotes,
            onCheckedChange = { checked ->
                onAppPreferencesChange(appPreferences.copy(animateEmotes = checked))
            }
        ) {
            Text(stringResource(R.string.animated_emotes))
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsSwitch(
            modifier = Modifier.padding(horizontal = 8.dp),
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
                .padding(horizontal = 8.dp),
            value = appPreferences.messageLimit.toFloat(),
            onValueChange = { value ->
                onAppPreferencesChange(appPreferences.copy(messageLimit = value.toInt()))
            },
            valueRange = 10f..1000f,
            steps = 10,
            valueContent = { value ->
                Text(
                    modifier = Modifier.width(48.dp),
                    text = "%,d".format(value.toInt())
                )
            }
        ) {
            Text(stringResource(R.string.message_limit))
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsSlider(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp),
            value = appPreferences.recentMsgLimit.toFloat(),
            onValueChange = { value ->
                onAppPreferencesChange(appPreferences.copy(recentMsgLimit = value.toInt()))
            },
            valueRange = 0f..500f,
            steps = 10,
            valueContent = { value ->
                if (value == 0f) {
                    Text(text = "Disabled")
                } else {
                    Text(
                        modifier = Modifier.width(48.dp),
                        text = "%,d".format(value.toInt())
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
                .padding(horizontal = 8.dp)
        ) {
            Text("Notifications")
        }

        SettingsLink(
            modifier = Modifier.padding(horizontal = 8.dp),
            onClick = {},
            onClickLabel = "Open notification settings"
        ) {
            Text(text = "Open notification settings")
        }

        SettingsLink(
            modifier = Modifier.padding(horizontal = 8.dp),
            onClick = {},
            onClickLabel = "Open bubble settings"
        ) {
            Text(text = "Open bubble settings")
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsHeader(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(stringResource(R.string.api_settings))
        }

        SettingsEdit(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp),
            value = appPreferences.helixClientId,
            placeholder = {
                Text(text = "your-client-id-here")
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
                .padding(horizontal = 8.dp),
            value = appPreferences.helixRedirect,
            placeholder = {
                Text(text = "https://localhost")
            },
            onValueChange = { value ->
                onAppPreferencesChange(appPreferences.copy(helixRedirect = value))
            }
        ) {
            Text(stringResource(R.string.api_helix_redirect))
        }
    }
}
