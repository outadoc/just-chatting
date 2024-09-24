package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.shared.MR

@Composable
internal fun MainNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    NavigationSuiteScaffold(
        modifier = modifier,
        content = {
            Scaffold(
                topBar = topBar,
                content = content,
            )
        },
        navigationSuiteItems = {
            item(
                selected = selectedScreen == Screen.Timeline,
                onClick = { onSelectedTabChange(Screen.Timeline) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(MR.strings.timeline_title)) },
            )

            item(
                selected = selectedScreen == Screen.Followed,
                onClick = { onSelectedTabChange(Screen.Followed) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(MR.strings.channels)) },
            )

            item(
                selected = selectedScreen == Screen.Search,
                onClick = { onSelectedTabChange(Screen.Search) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(MR.strings.search)) },
            )

            item(
                selected = selectedScreen == Screen.Settings.Root,
                onClick = { onSelectedTabChange(Screen.Settings.Root) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(MR.strings.settings)) },
            )
        },
    )
}
