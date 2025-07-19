package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.channels
import fr.outadoc.justchatting.shared.search
import fr.outadoc.justchatting.shared.settings
import fr.outadoc.justchatting.shared.timeline_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MainNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val navSuiteType: NavigationSuiteType =
        NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())

    NavigationSuiteScaffoldLayout(
        navigationSuite = {
            NavigationSuite(
                modifier = modifier,
                navigationSuiteType = navSuiteType,
                verticalArrangement = Arrangement.Center,
                content = {
                    NavigationSuiteItem(
                        selected = selectedScreen == Screen.Timeline,
                        onClick = { onSelectedTabChange(Screen.Timeline) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(Res.string.timeline_title)) },
                    )

                    NavigationSuiteItem(
                        selected = selectedScreen == Screen.Followed,
                        onClick = { onSelectedTabChange(Screen.Followed) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(Res.string.channels)) },
                    )

                    NavigationSuiteItem(
                        selected = selectedScreen == Screen.Search,
                        onClick = { onSelectedTabChange(Screen.Search) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(Res.string.search)) },
                    )

                    NavigationSuiteItem(
                        selected = selectedScreen == Screen.Settings,
                        onClick = { onSelectedTabChange(Screen.Settings) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(Res.string.settings)) },
                    )
                },
            )
        },
        content = {
            // TODO check if this is still needed
            Scaffold(
                topBar = topBar,
                content = content,
                contentWindowInsets = when (navSuiteType) {
                    NavigationSuiteType.NavigationBar -> {
                        WindowInsets.statusBars
                    }

                    else -> {
                        ScaffoldDefaults.contentWindowInsets
                    }
                },
            )
        },
    )
}
