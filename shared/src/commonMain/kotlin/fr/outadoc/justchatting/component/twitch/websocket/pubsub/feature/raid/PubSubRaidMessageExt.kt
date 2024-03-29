package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.raid

import fr.outadoc.justchatting.component.chatapi.common.Raid

internal fun PubSubRaidMessage.map(): Raid? {
    val imageDimensions = "70x70"
    return when (this) {
        PubSubRaidMessage.Cancel -> null

        is PubSubRaidMessage.Update -> Raid.Preparing(
            targetId = raid.targetId,
            targetLogin = raid.targetLogin,
            targetDisplayName = raid.targetDisplayName,
            targetProfileImageUrl = raid.targetProfileImageUrlTemplate
                ?.replace("%s", imageDimensions),
            viewerCount = raid.viewerCount,
        )

        is PubSubRaidMessage.Go -> Raid.Go(
            targetId = raid.targetId,
            targetLogin = raid.targetLogin,
            targetDisplayName = raid.targetDisplayName,
            targetProfileImageUrl = raid.targetProfileImageUrlTemplate
                ?.replace("%s", imageDimensions),
            viewerCount = raid.viewerCount,
        )
    }
}
