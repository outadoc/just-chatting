package fr.outadoc.justchatting.component.chatapi.common

import androidx.compose.runtime.Immutable

@Immutable
data class Raid(
    val targetId: String,
    val targetLogin: String,
    val targetDisplayName: String,
    val targetProfileImageUrl: String?,
    val transitionJitterSeconds: Int,
    val forceRaidNowSeconds: Int,
    val viewerCount: Int,
)
