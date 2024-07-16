package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.StreamAndUserInfo
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.ThemePreviews
import fr.outadoc.justchatting.utils.presentation.plus

@ThemePreviews
@Composable
internal fun SettingsListPreview() {
    AppTheme {
        SettingsList(
            loggedInUser = User(
                id = "123",
                displayName = "Maghla",
                login = "maghla",
                profileImageUrl = null,
            ),
            onLogoutClick = {},
            onOpenDependencyCredits = {},
            onOpenThirdPartiesSection = {},
            onOpenAppearanceSection = {},
            onOpenAboutSection = {},
            onOpenNotificationSection = {},
        )
    }
}

@Composable
internal fun SettingsList(
    modifier: Modifier = Modifier,
    loggedInUser: User?,
    onLogoutClick: () -> Unit,
    onOpenDependencyCredits: () -> Unit,
    onOpenThirdPartiesSection: () -> Unit,
    onOpenAboutSection: () -> Unit,
    onOpenAppearanceSection: () -> Unit,
    onOpenNotificationSection: () -> Unit,
    itemInsets: PaddingValues = SettingsConstants.ItemInsets,
    insets: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = insets + PaddingValues(bottom = 16.dp),
    ) {
        item {
            var showLogoutDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.padding(8.dp),
            ) {
                AnimatedVisibility(visible = loggedInUser != null) {
                    StreamAndUserInfo(
                        modifier = Modifier
                            .padding(itemInsets)
                            .padding(top = 16.dp),
                        user = loggedInUser,
                        stream = null,
                    )
                }

                SettingsText(
                    modifier = Modifier.padding(itemInsets),
                    onClick = { showLogoutDialog = true },
                    onClickLabel = null,
                    title = {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.error,
                        ) {
                            Text(text = stringResource(MR.strings.settings_account_logout_action))
                        }
                    },
                )
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text(text = stringResource(MR.strings.logout_title)) },
                    text = {
                        Text(
                            text = stringResource(
                                MR.strings.logout_msg,
                                loggedInUser?.displayName ?: "",
                            ),
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text(text = stringResource(MR.strings.no))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onLogoutClick()
                                showLogoutDialog = false
                            },
                        ) {
                            Text(text = stringResource(MR.strings.yes))
                        }
                    },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            ListItem(
                modifier = Modifier.clickable { onOpenThirdPartiesSection() },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(MR.strings.settings_thirdparty_section_title))
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            ListItem(
                modifier = Modifier.clickable { onOpenNotificationSection() },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(MR.strings.settings_notifications_header))
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            ListItem(
                modifier = Modifier.clickable { onOpenAppearanceSection() },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(MR.strings.settings_appearance_header))
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            ListItem(
                modifier = Modifier.clickable { onOpenDependencyCredits() },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(MR.strings.settings_dependencies_title))
                },
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable { onOpenAboutSection() },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(MR.strings.settings_about_header))
                },
            )
        }
    }
}
