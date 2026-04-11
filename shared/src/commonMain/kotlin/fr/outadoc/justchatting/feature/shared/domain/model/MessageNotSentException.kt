package fr.outadoc.justchatting.feature.shared.domain.model

public class MessageNotSentException(
    message: String,
    public val dropReasonCode: String? = null,
    public val dropReasonMessage: String? = null,
) : Exception(message)
