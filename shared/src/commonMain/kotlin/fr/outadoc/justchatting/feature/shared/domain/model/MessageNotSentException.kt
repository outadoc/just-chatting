package fr.outadoc.justchatting.feature.shared.domain.model

internal class MessageNotSentException(
    message: String,
    val dropReasonCode: String? = null,
    val dropReasonMessage: String? = null,
) : Exception(message)
