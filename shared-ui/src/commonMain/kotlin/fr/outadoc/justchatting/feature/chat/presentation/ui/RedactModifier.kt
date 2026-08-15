package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

/**
 * If [redact] is true, blurs the content this modifier is applied to.
 */
internal fun Modifier.redactable(redact: Boolean = true): Modifier =
    if (redact) {
        blur(
            radius = 6.dp,
            edgeTreatment = BlurredEdgeTreatment.Unbounded,
        )
    } else {
        this
    }
