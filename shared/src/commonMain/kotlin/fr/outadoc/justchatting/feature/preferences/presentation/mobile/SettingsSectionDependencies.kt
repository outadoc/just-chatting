package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.preferences.presentation.Dependency
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.all_goBack
import fr.outadoc.justchatting.shared.settings_dependencies_cd
import fr.outadoc.justchatting.shared.settings_dependencies_header
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.plus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionDependencies(
    modifier: Modifier = Modifier,
    canNavigateUp: Boolean = true,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_dependencies_header)) },
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
        SettingsSectionDependenciesContent(
            modifier = modifier,
            insets = insets,
            readDependencies = koinInject(),
        )
    }
}

@Composable
private fun SettingsSectionDependenciesContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    itemInsets: PaddingValues = SettingsConstants.ItemInsets,
    readDependencies: ReadExternalDependenciesList,
) {
    val uriHandler = LocalUriHandler.current
    var deps: List<Dependency> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(readDependencies) {
        deps = readDependencies()
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = insets + PaddingValues(bottom = 16.dp),
    ) {
        items(deps) { dependency ->
            SettingsText(
                modifier = Modifier.padding(itemInsets),
                onClick = {
                    dependency.moduleUrl?.let { url ->
                        uriHandler.openUri(url)
                    }
                },
                onClickLabel = stringResource(Res.string.settings_dependencies_cd)
                    .takeIf { dependency.moduleUrl != null },
                title = { Text(text = dependency.moduleName) },
                subtitle = {
                    dependency.moduleLicense?.let { license ->
                        Text(text = license)
                    }
                },
                trailingIcon = {
                    if (dependency.moduleUrl != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    }
}
