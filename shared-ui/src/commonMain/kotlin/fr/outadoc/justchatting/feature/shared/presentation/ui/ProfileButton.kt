package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.profile_settings_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shows the logged-in user's avatar, and opens the settings when clicked.
 */
@Composable
internal fun ProfileButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    ProfileButton(
        modifier = modifier,
        profileImageUrl = state.user?.profileImageUrl,
        onClick = onClick,
    )
}

@Composable
internal fun ProfileButton(
    modifier: Modifier = Modifier,
    profileImageUrl: String?,
    onClick: () -> Unit,
) {
    AccessibleIconButton(
        modifier = modifier,
        onClick = onClick,
        onClickLabel = stringResource(Res.string.profile_settings_cd),
    ) {
        UserAvatar(
            profileImageUrl = profileImageUrl,
            size = 32.dp,
            shape = UserAvatarDefaults.SmallShape,
        )
    }
}
