package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable

@Immutable
public sealed class Raid {
    public data class Preparing(
        val targetId: String,
        val targetLogin: String,
        val targetDisplayName: String,
        val targetProfileImageUrl: String?,
        val viewerCount: Int,
    ) : Raid()

    public data class Go(
        val targetId: String,
        val targetLogin: String,
        val targetDisplayName: String,
        val targetProfileImageUrl: String?,
        val viewerCount: Int,
    ) : Raid()
}
