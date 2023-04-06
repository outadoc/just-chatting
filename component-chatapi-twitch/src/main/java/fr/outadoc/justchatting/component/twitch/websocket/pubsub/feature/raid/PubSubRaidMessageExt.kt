package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.raid

import fr.outadoc.justchatting.component.chatapi.common.Raid

internal fun PubSubRaidMessage.Raid.map(): Raid {
    return Raid(
        targetId = targetId,
        targetLogin = targetLogin,
        targetDisplayName = targetDisplayName,
        targetProfileImageUrl = targetProfileImageUrl,
        transitionJitterSeconds = transitionJitterSeconds,
        forceRaidNowSeconds = forceRaidNowSeconds,
        viewerCount = viewerCount,
    )
}
