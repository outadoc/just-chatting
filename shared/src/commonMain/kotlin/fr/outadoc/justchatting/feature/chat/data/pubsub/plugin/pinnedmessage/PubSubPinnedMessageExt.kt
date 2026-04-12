package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.pinnedmessage

import fr.outadoc.justchatting.feature.chat.domain.model.PinnedMessage

internal fun PubSubPinnedMessage.Pin.map(): PinnedMessage = PinnedMessage(
    pinId = data.pinId,
    pinnedBy = data.pinnedBy.map(),
    message = data.message.map(),
)

private fun PubSubPinnedMessage.Pin.Message.map(): PinnedMessage.Message = PinnedMessage.Message(
    messageId = messageId,
    sender = sender.map(),
    content = content.map(),
    startsAt = startsAt,
    endsAt = endsAt,
)

private fun PubSubPinnedMessage.Pin.Message.Content.map(): PinnedMessage.Message.Content = PinnedMessage.Message.Content(
    text = text,
)

private fun PubSubPinnedMessage.Pin.User.map(): PinnedMessage.User = PinnedMessage.User(
    userId = userId,
    displayName = displayName,
)
