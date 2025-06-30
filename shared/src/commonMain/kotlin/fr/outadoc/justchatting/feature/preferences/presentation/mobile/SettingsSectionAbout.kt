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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.all_goBack
import fr.outadoc.justchatting.shared.app_license_name
import fr.outadoc.justchatting.shared.app_license_url
import fr.outadoc.justchatting.shared.app_name
import fr.outadoc.justchatting.shared.app_repo_name
import fr.outadoc.justchatting.shared.app_repo_url
import fr.outadoc.justchatting.shared.settings_about_header
import fr.outadoc.justchatting.shared.settings_about_license_cd
import fr.outadoc.justchatting.shared.settings_about_license_subtitle
import fr.outadoc.justchatting.shared.settings_about_license_title
import fr.outadoc.justchatting.shared.settings_about_repo_cd
import fr.outadoc.justchatting.shared.settings_about_repo_title
import fr.outadoc.justchatting.shared.settings_about_version
import fr.outadoc.justchatting.shared.settings_about_xtra_subtitle
import fr.outadoc.justchatting.shared.settings_about_xtra_title
import fr.outadoc.justchatting.shared.settings_logs_subtitle
import fr.outadoc.justchatting.shared.settings_logs_title
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
    ) { insets ->
        SettingsSectionAboutContent(
            modifier = modifier,
            insets = insets,
            itemInsets = PaddingValues(horizontal = 16.dp),
        )
    }
}

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
                onClick = viewModel::onShareLogsClick,
                subtitle = { Text(text = stringResource(Res.string.settings_logs_subtitle)) },
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
