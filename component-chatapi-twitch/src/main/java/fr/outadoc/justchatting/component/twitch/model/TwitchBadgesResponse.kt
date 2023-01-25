package fr.outadoc.justchatting.component.twitch.model

import com.google.gson.annotations.SerializedName

data class TwitchBadgesResponse(
    @SerializedName("badge_sets")
    val badgeSets: Map<String, TwitchBadgeSet>
)
