package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * If [redact] is true, blurs the content this modifier is applied to.
 * When pressed, the contents will un-blur temporarily.
 */
internal fun Modifier.redactable(redact: Boolean = true): Modifier = composed {
    var overrideRedaction: Boolean by remember { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        if (overrideRedaction) 0.dp else 6.dp,
        label = "redaction radius",
    )

    if (redact) {
        Modifier
            .blur(
                radius = blurRadius,
                edgeTreatment = BlurredEdgeTreatment.Unbounded,
            ).pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            overrideRedaction = true
                            awaitRelease()
                        } finally {
                            overrideRedaction = false
                        }
                    },
                )
            }
    } else {
        Modifier
    }
}
