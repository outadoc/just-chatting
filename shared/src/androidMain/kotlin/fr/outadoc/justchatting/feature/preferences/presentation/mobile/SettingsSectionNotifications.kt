package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.MR
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionNotifications(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit = {},
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.settings_appearance_header)) },
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
        SettingsSectionNotificationsContent(
            modifier = modifier,
            insets = insets,
            itemInsets = PaddingValues(horizontal = 16.dp),
            onOpenNotificationPreferences = onOpenNotificationPreferences,
            onOpenBubblePreferences = onOpenBubblePreferences,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class, KoinExperimentalAPI::class)
@Composable
private fun SettingsSectionNotificationsContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    itemInsets: PaddingValues = SettingsConstants.ItemInsets,
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val appPreferences = state.appPreferences

    LazyColumn(
        modifier = modifier,
        contentPadding = insets,
    ) {
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

                    viewModel.updatePreferences(
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
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
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
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}
