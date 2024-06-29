package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed class Raid {

    data class Preparing(
        val targetId: String,
        val targetLogin: String,
        val targetDisplayName: String,
        val targetProfileImageUrl: String?,
        val viewerCount: Int,
    ) : Raid()

    data class Go(
        val targetId: String,
        val targetLogin: String,
        val targetDisplayName: String,
        val targetProfileImageUrl: String?,
        val viewerCount: Int,
    ) : Raid()
}
