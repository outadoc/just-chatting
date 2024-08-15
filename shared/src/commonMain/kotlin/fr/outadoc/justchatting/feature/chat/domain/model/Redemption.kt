package fr.outadoc.justchatting.feature.chat.domain.model

import kotlinx.datetime.Instant

internal data class Redemption(
    val id: String,
    val userId: String,
    val userAddedMessage: String? = null,
    val redeemedAt: Instant? = null,
    val reward: Reward,
)
