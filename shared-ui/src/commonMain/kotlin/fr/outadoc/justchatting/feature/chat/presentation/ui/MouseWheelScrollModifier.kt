package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val DefaultMouseWheelScrollStep = 40.dp

/**
 * A horizontally scrollable list otherwise only responds to horizontal drag/fling gestures;
 * this forwards the vertical delta of a mouse wheel scroll to [state] as well.
 */
internal fun Modifier.horizontalMouseWheelScroll(
    state: ScrollableState,
    step: Dp = DefaultMouseWheelScrollStep,
): Modifier =
    composed {
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current

        pointerInput(state) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val scrollDelta =
                            event.changes
                                .firstOrNull()
                                ?.scrollDelta
                                ?.y ?: 0f
                        if (scrollDelta != 0f) {
                            scope.launch {
                                state.scrollBy(scrollDelta * with(density) { step.toPx() })
                            }
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
        }
    }
