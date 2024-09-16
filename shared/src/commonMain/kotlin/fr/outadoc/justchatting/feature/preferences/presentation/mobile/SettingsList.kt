package fr.outadoc.justchatting.feature.preferences.presentation.mobile

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
import fr.outadoc.justchatting.feature.chat.presentation.mobile.UserInfo
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.core.PlaceholderHighlight
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.material3.placeholder
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.material3.shimmer
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.areBubblesSupported
import fr.outadoc.justchatting.utils.presentation.plus
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                modifier = Modifier.padding(itemInsets),
            ) {
                val placeholderUser = remember {
                    User(
                        id = "",
                        login = "",
                        displayName = "",
                        description = "",
                        profileImageUrl = "",
                        createdAt = Instant.fromEpochMilliseconds(0),
                        usedAt = Instant.fromEpochMilliseconds(0),
                    )
                }

                UserInfo(
                    modifier = Modifier
                        .padding(itemInsets)
                        .padding(top = 16.dp)
                        .placeholder(
                            visible = loggedInUser == null,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            highlight = PlaceholderHighlight.shimmer(),
                        ),
                    user = loggedInUser ?: placeholderUser,
                )

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
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { onOpenThirdPartiesSection() },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(stringResource(MR.strings.settings_thirdparty_section_title))
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (areBubblesSupported()) {
            item {
                SettingsText(
                    modifier = Modifier.padding(itemInsets),
                    onClick = { onOpenNotificationSection() },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(stringResource(MR.strings.settings_notifications_header))
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { onOpenAppearanceSection() },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(stringResource(MR.strings.settings_appearance_header))
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { onOpenDependencyCredits() },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(stringResource(MR.strings.settings_dependencies_header))
                },
            )
        }

        item {
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = { onOpenAboutSection() },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(stringResource(MR.strings.settings_about_header))
                },
            )
        }
    }
}

@Preview
@Composable
internal fun SettingsListPreview() {
    AppTheme {
        SettingsList(
            loggedInUser = User(
                id = "123",
                login = "maghla",
                displayName = "Maghla",
                description = "",
                profileImageUrl = "",
                createdAt = Instant.DISTANT_PAST,
                usedAt = Instant.DISTANT_PAST,
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
