package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.all_goBack
import fr.outadoc.justchatting.shared.internal.settings_appearance_animatedEmotes_title
import fr.outadoc.justchatting.shared.internal.settings_appearance_animatedGifs_title
import fr.outadoc.justchatting.shared.internal.settings_appearance_header
import fr.outadoc.justchatting.shared.internal.settings_appearance_timestamps_title
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionAppearance(
    modifier: Modifier = Modifier,
    canNavigateUp: Boolean = true,
    onNavigateUp: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_appearance_header)) },
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
        SettingsSectionAppearanceContent(
            modifier = modifier,
            insets = insets,
            itemInsets = PaddingValues(horizontal = 16.dp),
        )
    }
}

@Composable
private fun SettingsSectionAppearanceContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    itemInsets: PaddingValues = SettingsConstants.ItemInsets,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val appPreferences = state.appPreferences

    LazyColumn(
        modifier = modifier,
        contentPadding = insets,
    ) {
        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.showTimestamps,
                onCheckedChange = { checked ->
                    viewModel.updatePreferences(appPreferences.copy(showTimestamps = checked))
                },
                title = {
                    Text(stringResource(Res.string.settings_appearance_timestamps_title))
                },
            )
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.showAnimatedEmotes,
                onCheckedChange = { checked ->
                    viewModel.updatePreferences(appPreferences.copy(showAnimatedEmotes = checked))
                },
                title = {
                    Text(stringResource(Res.string.settings_appearance_animatedEmotes_title))
                },
            )
        }

        item {
            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                checked = appPreferences.showAnimatedGifs,
                onCheckedChange = { checked ->
                    viewModel.updatePreferences(appPreferences.copy(showAnimatedGifs = checked))
                },
                title = {
                    Text(stringResource(Res.string.settings_appearance_animatedGifs_title))
                },
            )
        }
    }
}
