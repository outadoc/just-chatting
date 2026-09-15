package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.BubblePermission
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.all_goBack
import fr.outadoc.justchatting.shared.internal.settings_appearance_header
import fr.outadoc.justchatting.shared.internal.settings_notifications_bubbles_allowed
import fr.outadoc.justchatting.shared.internal.settings_notifications_bubbles_disabled
import fr.outadoc.justchatting.shared.internal.settings_notifications_bubbles_selectedOnly
import fr.outadoc.justchatting.shared.internal.settings_notifications_enable_subtitle
import fr.outadoc.justchatting.shared.internal.settings_notifications_enable_title
import fr.outadoc.justchatting.shared.internal.settings_notifications_openBubbleSettings
import fr.outadoc.justchatting.shared.internal.settings_notifications_openNotificationsSettings
import fr.outadoc.justchatting.utils.permissions.PermissionState
import fr.outadoc.justchatting.utils.permissions.isGranted
import fr.outadoc.justchatting.utils.permissions.rememberPermissionState
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.OnLifecycleEvent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionNotifications(
    modifier: Modifier = Modifier,
    canNavigateUp: Boolean = true,
    onNavigateUp: () -> Unit = {},
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
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
        SettingsSectionNotificationsContent(
            modifier = modifier,
            insets = insets,
            itemInsets = PaddingValues(horizontal = 16.dp),
            onOpenNotificationPreferences = onOpenNotificationPreferences,
            onOpenBubblePreferences = onOpenBubblePreferences,
        )
    }
}

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

    // Bubbles are gated by a per-app OS setting the app can read but not change. When it is
    // anything but "all conversations", bubbles silently never appear, so say so here instead of
    // leaving the user to work it out. Re-read on resume: the row above sends them to the system
    // settings to change exactly this, and they come back expecting to see the new value.
    val notifier: ChatNotifier = koinInject()
    var bubblePermission by remember { mutableStateOf(notifier.bubblePermission) }

    OnLifecycleEvent(
        onResume = {
            bubblePermission = notifier.bubblePermission
        },
    )

    val bubbleStatus: StringResource? =
        when (bubblePermission) {
            BubblePermission.Unsupported -> null
            BubblePermission.Disabled -> Res.string.settings_notifications_bubbles_disabled
            BubblePermission.SelectedConversationsOnly -> Res.string.settings_notifications_bubbles_selectedOnly
            BubblePermission.AllConversations -> Res.string.settings_notifications_bubbles_allowed
        }

    LazyColumn(
        modifier = modifier,
        contentPadding = insets,
    ) {
        item {
            val notificationPermissionState: PermissionState =
                rememberPermissionState("android.permission.POST_NOTIFICATIONS")

            SettingsSwitch(
                modifier = Modifier.padding(itemInsets),
                title = { Text(text = stringResource(Res.string.settings_notifications_enable_title)) },
                subtitle = { Text(text = stringResource(Res.string.settings_notifications_enable_subtitle)) },
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
                onClickLabel = stringResource(Res.string.settings_notifications_openNotificationsSettings),
                title = {
                    Text(text = stringResource(Res.string.settings_notifications_openNotificationsSettings))
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
                onClick = onOpenBubblePreferences,
                onClickLabel = stringResource(Res.string.settings_notifications_openBubbleSettings),
                title = {
                    Text(text = stringResource(Res.string.settings_notifications_openBubbleSettings))
                },
                subtitle = {
                    if (bubbleStatus != null) {
                        Text(text = stringResource(bubbleStatus))
                    }
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
