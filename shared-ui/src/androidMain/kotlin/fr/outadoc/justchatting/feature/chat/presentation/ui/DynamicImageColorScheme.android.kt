package fr.outadoc.justchatting.feature.chat.presentation.ui

import coil3.request.ImageRequest
import coil3.request.allowHardware

internal actual val enableColorTransitions: Boolean
    get() = true

internal actual fun ImageRequest.Builder.allowSoftwareRendering(): ImageRequest.Builder = allowHardware(false)
