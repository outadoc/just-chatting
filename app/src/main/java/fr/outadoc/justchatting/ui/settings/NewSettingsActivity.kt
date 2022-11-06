package fr.outadoc.justchatting.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.composepreview.ScreenPreviews

class NewSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingsList()
        }
    }
}

@ScreenPreviews
@Composable
fun SettingsScreenPreview() {
    Mdc3Theme {
        SettingsScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackPress: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.settings))
                }
            )
        }
    ) { insets ->
        SettingsList(
            modifier = Modifier.padding(insets)
        )
    }
}

@Composable
fun SettingsList(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.verticalScroll(scrollState)) {
        SettingsHeader(modifier = Modifier.padding(8.dp)) {
            Text(stringResource(R.string.settings_chat))
        }

        SettingsSwitch(
            modifier = Modifier.padding(horizontal = 8.dp),
            checked = true,
            onCheckedChange = {}
        ) {
            Text(stringResource(R.string.animated_emotes))
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsSwitch(
            modifier = Modifier.padding(horizontal = 8.dp),
            checked = true,
            onCheckedChange = {}
        ) {
            Text(stringResource(R.string.timestamps))
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        SettingsSlider(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp),
            value = 1f,
            onValueChange = {},
            valueRange = 10f..1000f,
            steps = 10,
            valueContent = { value ->
                Text(
                    modifier = Modifier.width(32.dp),
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
            value = 0f,
            onValueChange = {},
            valueRange = 0f..1000f,
            steps = 10,
            valueContent = { value ->
                if (value == 0f) {
                    Text(text = "Disabled")
                } else {
                    Text(
                        modifier = Modifier.width(32.dp),
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
            value = "",
            placeholder = {
                Text(text = "your-api-token-here")
            },
            onValueChange = {}
        ) {
            Text(stringResource(R.string.api_helix))
        }

        SettingsEdit(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp),
            value = "",
            placeholder = {
                Text(text = "https://localhost")
            },
            onValueChange = {}
        ) {
            Text(stringResource(R.string.api_helix_redirect))
        }
    }
}
