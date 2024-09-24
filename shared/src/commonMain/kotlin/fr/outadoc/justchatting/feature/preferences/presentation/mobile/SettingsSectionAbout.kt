package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionAbout(
    modifier: Modifier = Modifier,
    canNavigateUp: Boolean = true,
    onNavigateUp: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.settings_about_header)) },
                navigationIcon = {
                    if (canNavigateUp) {
                        IconButton(
                            onClick = onNavigateUp,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(MR.strings.all_goBack),
                            )
                        }
                    }
                },
            )
        },
    ) { insets ->
        SettingsSectionAboutContent(
            modifier = modifier,
            insets = insets,
            itemInsets = PaddingValues(horizontal = 16.dp),
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun SettingsSectionAboutContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    itemInsets: PaddingValues = SettingsConstants.ItemInsets,
) {
    val uriHandler = LocalUriHandler.current
    val viewModel: SettingsViewModel = koinViewModel()

    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = modifier,
        contentPadding = insets,
    ) {
        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(MR.strings.app_name)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            MR.strings.settings_about_version,
                            state.appVersionName.orEmpty(),
                        ),
                    )
                },
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
                onClickLabel = stringResource(MR.strings.settings_about_license_cd),
                title = { Text(text = stringResource(MR.strings.settings_about_xtra_title)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            MR.strings.settings_about_xtra_subtitle,
                            stringResource(MR.strings.app_name),
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
            val repoUrl = stringResource(MR.strings.app_repo_url)
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { uriHandler.openUri(repoUrl) },
                onClickLabel = stringResource(MR.strings.settings_about_repo_cd),
                title = { Text(text = stringResource(MR.strings.settings_about_repo_title)) },
                subtitle = { Text(text = stringResource(MR.strings.app_repo_name)) },
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
                title = { Text(text = stringResource(MR.strings.settings_logs_title)) },
                onClick = viewModel::onShareLogsClick,
                subtitle = { Text(text = stringResource(MR.strings.settings_logs_subtitle)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
