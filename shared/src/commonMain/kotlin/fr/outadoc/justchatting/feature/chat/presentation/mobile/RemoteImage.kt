package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.runtime.Composable
import coil3.request.ImageRequest

@Composable
internal expect fun remoteImageModel(url: String?): ImageRequest
