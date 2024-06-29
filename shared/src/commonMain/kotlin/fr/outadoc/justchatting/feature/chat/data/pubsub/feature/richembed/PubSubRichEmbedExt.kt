package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.richembed

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent

internal fun PubSubRichEmbedMessage.RichEmbed.Data.map(): ChatEvent.RichEmbed =
    ChatEvent.RichEmbed(
        messageId = messageId,
        title = title,
        requestUrl = requestUrl,
        thumbnailUrl = thumbnailUrl,
        authorName = authorName,
        channelName = metadata.clipMetadata?.channelDisplayName,
    )
