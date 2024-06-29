package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.pinnedmessage

import fr.outadoc.justchatting.feature.chat.domain.model.PinnedMessage

internal fun PubSubPinnedMessage.Pin.map(): PinnedMessage {
    return PinnedMessage(
        pinId = data.pinId,
        pinnedBy = data.pinnedBy.map(),
        message = data.message.map(),
    )
}

private fun PubSubPinnedMessage.Pin.Message.map(): PinnedMessage.Message {
    return PinnedMessage.Message(
        messageId = messageId,
        sender = sender.map(),
        content = content.map(),
        startsAt = startsAt,
        endsAt = endsAt,
    )
}

private fun PubSubPinnedMessage.Pin.Message.Content.map(): PinnedMessage.Message.Content {
    return PinnedMessage.Message.Content(
        text = text,
    )
}

private fun PubSubPinnedMessage.Pin.User.map(): PinnedMessage.User {
    return PinnedMessage.User(
        userId = userId,
        displayName = displayName,
    )
}
