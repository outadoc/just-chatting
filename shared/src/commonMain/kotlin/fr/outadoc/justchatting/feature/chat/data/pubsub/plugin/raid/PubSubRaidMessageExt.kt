package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.raid

import fr.outadoc.justchatting.feature.chat.domain.model.Raid

internal fun PubSubRaidMessage.map(): Raid? {
    val imageDimensions = "70x70"
    return when (this) {
        PubSubRaidMessage.Cancel -> {
            null
        }

        is PubSubRaidMessage.Update -> {
            Raid.Preparing(
                targetId = raid.targetId,
                targetLogin = raid.targetLogin,
                targetDisplayName = raid.targetDisplayName,
                targetProfileImageUrl =
                raid.targetProfileImageUrlTemplate
                    ?.replace("%s", imageDimensions),
                viewerCount = raid.viewerCount,
            )
        }

        is PubSubRaidMessage.Go -> {
            Raid.Go(
                targetId = raid.targetId,
                targetLogin = raid.targetLogin,
                targetDisplayName = raid.targetDisplayName,
                targetProfileImageUrl =
                raid.targetProfileImageUrlTemplate
                    ?.replace("%s", imageDimensions),
                viewerCount = raid.viewerCount,
            )
        }
    }
}
