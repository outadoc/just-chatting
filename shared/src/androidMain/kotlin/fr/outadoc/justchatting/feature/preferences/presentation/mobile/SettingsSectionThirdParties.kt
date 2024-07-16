package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.MR
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionThirdParties(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.settings_thirdparty_section_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(MR.strings.all_goBack),
                        )
                    }
                },
            )
        },
    ) { insets ->
        SettingsSectionThirdPartiesContent(
            modifier = modifier,
            insets = insets,
            itemInsets = PaddingValues(horizontal = 16.dp),
        )
    }
}

@Composable
private fun SettingsSectionThirdPartiesContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    itemInsets: PaddingValues = SettingsConstants.ItemInsets,
) {
    val uriHandler = LocalUriHandler.current

    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val appPreferences = state.appPreferences

    LazyColumn(
        modifier = modifier,
        contentPadding = insets,
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
                    viewModel.updatePreferences(appPreferences.copy(enableRecentMessages = checked))
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
                    viewModel.updatePreferences(appPreferences.copy(enablePronouns = checked))
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
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
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
                    viewModel.updatePreferences(appPreferences.copy(enableBttvEmotes = checked))
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
                    viewModel.updatePreferences(appPreferences.copy(enableFfzEmotes = checked))
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
                    viewModel.updatePreferences(appPreferences.copy(enableStvEmotes = checked))
                },
                title = {
                    Text(stringResource(MR.strings.settings_thirdparty_stv_title))
                },
                subtitle = {
                    Text(stringResource(MR.strings.settings_thirdparty_stv_subtitle))
                },
            )
        }
    }
}
