package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun IntervalCheckVisibility(
    modifier: Modifier = Modifier,
    enter: EnterTransition,
    exit: ExitTransition,
    visible: () -> Boolean,
    content: @Composable () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        while (isActive) {
            isVisible = visible()
            delay(1.seconds)
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        enter = enter,
        exit = exit,
        visible = isVisible,
    ) {
        content()
    }
}
