package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.raid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class PubSubRaidMessage {

    @Serializable
    @SerialName("raid_update_v2")
    data class Update(
        @SerialName("raid")
        val raid: Raid,
    ) : PubSubRaidMessage()

    @Serializable
    @SerialName("raid_go_v2")
    data class Go(
        @SerialName("raid")
        val raid: Raid,
    ) : PubSubRaidMessage()

    @Serializable
    @SerialName("raid_cancel_v2")
    data object Cancel : PubSubRaidMessage()

    @Serializable
    data class Raid(
        @SerialName("id")
        val raidId: String,
        @SerialName("target_id")
        val targetId: String,
        @SerialName("target_login")
        val targetLogin: String,
        @SerialName("target_display_name")
        val targetDisplayName: String,
        @SerialName("target_profile_image")
        val targetProfileImageUrlTemplate: String?,
        @SerialName("transition_jitter_seconds")
        val transitionJitterSeconds: Int,
        @SerialName("force_raid_now_seconds")
        val forceRaidNowSeconds: Int,
        @SerialName("viewer_count")
        val viewerCount: Int,
    )
}
