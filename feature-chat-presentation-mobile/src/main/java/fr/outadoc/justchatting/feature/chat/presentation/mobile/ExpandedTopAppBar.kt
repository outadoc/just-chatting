package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = LocalContentColor.current,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    secondRow: @Composable () -> Unit = {},
) {
    val appBarContainerColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "Container color",
    )

    Surface(
        modifier = modifier,
        color = appBarContainerColor,
        contentColor = contentColor,
    ) {
        Column {
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor,
                ),
                scrollBehavior = scrollBehavior,
            )

            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                secondRow()
            }
        }
    }
}
