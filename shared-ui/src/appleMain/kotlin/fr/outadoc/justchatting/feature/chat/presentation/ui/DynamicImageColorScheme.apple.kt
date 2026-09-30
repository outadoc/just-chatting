package fr.outadoc.justchatting.feature.chat.presentation.ui

import coil3.request.ImageRequest

internal actual val enableColorTransitions: Boolean
    get() = true

internal actual fun ImageRequest.Builder.allowSoftwareRendering(): ImageRequest.Builder = this
