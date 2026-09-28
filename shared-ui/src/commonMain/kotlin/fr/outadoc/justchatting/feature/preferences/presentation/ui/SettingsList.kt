package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.ui.ExtraUserInfo
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListDefaults
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListItem
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatar
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatarDefaults
import fr.outadoc.justchatting.feature.shared.presentation.ui.placeholder.core.PlaceholderHighlight
import fr.outadoc.justchatting.feature.shared.presentation.ui.placeholder.material3.placeholder
import fr.outadoc.justchatting.feature.shared.presentation.ui.placeholder.material3.shimmer
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.logout_msg
import fr.outadoc.justchatting.shared.internal.logout_title
import fr.outadoc.justchatting.shared.internal.no
import fr.outadoc.justchatting.shared.internal.settings_about_header
import fr.outadoc.justchatting.shared.internal.settings_account_logout_action
import fr.outadoc.justchatting.shared.internal.settings_appearance_header
import fr.outadoc.justchatting.shared.internal.settings_dependencies_header
import fr.outadoc.justchatting.shared.internal.settings_notifications_header
import fr.outadoc.justchatting.shared.internal.settings_thirdparty_section_title
import fr.outadoc.justchatting.shared.internal.yes
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.areBubblesSupported
import fr.outadoc.justchatting.utils.presentation.plus
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
public fun SettingsList(
    modifier: Modifier = Modifier,
    loggedInUser: User?,
    appVersionName: String? = null,
    onLogoutClick: () -> Unit,
    onOpenDependencyCredits: () -> Unit,
    onOpenThirdPartiesSection: () -> Unit,
    onOpenAboutSection: () -> Unit,
    onOpenAppearanceSection: () -> Unit,
    onOpenNotificationSection: () -> Unit,
    insets: PaddingValues = PaddingValues(),
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val menuItems: List<SettingsMenuEntry> =
        buildList {
            add(
                SettingsMenuEntry(
                    icon = Icons.Outlined.Extension,
                    title = Res.string.settings_thirdparty_section_title,
                    onClick = onOpenThirdPartiesSection,
                ),
            )

            if (areBubblesSupported()) {
                add(
                    SettingsMenuEntry(
                        icon = Icons.Outlined.Notifications,
                        title = Res.string.settings_notifications_header,
                        onClick = onOpenNotificationSection,
                    ),
                )
            }

            add(
                SettingsMenuEntry(
                    icon = Icons.Outlined.Palette,
                    title = Res.string.settings_appearance_header,
                    onClick = onOpenAppearanceSection,
                ),
            )

            add(
                SettingsMenuEntry(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = Res.string.settings_dependencies_header,
                    onClick = onOpenDependencyCredits,
                ),
            )

            add(
                SettingsMenuEntry(
                    icon = Icons.Outlined.Info,
                    title = Res.string.settings_about_header,
                    trailingText = appVersionName,
                    onClick = onOpenAboutSection,
                ),
            )
        }

    LazyColumn(
        modifier = modifier,
        contentPadding =
            insets +
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp,
                ),
    ) {
        item(key = "profile") {
            ProfileCard(
                modifier = Modifier.fillMaxWidth(),
                user = loggedInUser,
            )
        }

        item(key = "profile_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }

        itemsIndexed(
            items = menuItems,
            key = { _, entry -> entry.title.key },
        ) { index, entry ->
            SettingsMenuItem(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom =
                                if (index < menuItems.lastIndex) {
                                    SegmentedListDefaults.ItemSpacing
                                } else {
                                    0.dp
                                },
                        ),
                shape = SegmentedListDefaults.shape(index = index, count = menuItems.size),
                icon = entry.icon,
                title = stringResource(entry.title),
                trailingText = entry.trailingText,
                onClick = entry.onClick,
            )
        }

        item(key = "logout_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item(key = "logout") {
            SettingsMenuItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Outlined.Logout,
                title = stringResource(Res.string.settings_account_logout_action),
                contentColor = MaterialTheme.colorScheme.error,
                onClick = { showLogoutDialog = true },
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = stringResource(Res.string.logout_title)) },
            text = {
                Text(
                    text =
                        stringResource(
                            Res.string.logout_msg,
                            loggedInUser?.displayName ?: "",
                        ),
                )
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = stringResource(Res.string.no))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogoutClick()
                        showLogoutDialog = false
                    },
                ) {
                    Text(text = stringResource(Res.string.yes))
                }
            },
        )
    }
}

private data class SettingsMenuEntry(
    val icon: ImageVector,
    val title: StringResource,
    val trailingText: String? = null,
    val onClick: () -> Unit,
)

@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    user: User?,
) {
    val placeholderUser =
        remember {
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

    val displayedUser = user ?: placeholderUser

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(20.dp)
                    .placeholder(
                        visible = user == null,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        highlight = PlaceholderHighlight.shimmer(),
                    ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                UserAvatar(
                    profileImageUrl = displayedUser.profileImageUrl,
                    size = UserAvatarDefaults.LargeSize,
                    shape = UserAvatarDefaults.LargeShape,
                )

                Column {
                    Text(
                        text = displayedUser.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (displayedUser.login.isNotEmpty()) {
                        Text(
                            text = "@${displayedUser.login}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            ExtraUserInfo(user = displayedUser)
        }
    }
}

@Composable
private fun SettingsMenuItem(
    modifier: Modifier = Modifier,
    shape: Shape = SegmentedListDefaults.StandaloneShape,
    icon: ImageVector,
    title: String,
    trailingText: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        modifier = modifier,
        shape = shape,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    if (contentColor == MaterialTheme.colorScheme.onSurface) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        contentColor
                    },
            )

            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )

            trailingText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
internal fun SettingsListPreview() {
    AppTheme {
        SettingsList(
            loggedInUser =
                User(
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
            appVersionName = "1.0.0",
        )
    }
}
