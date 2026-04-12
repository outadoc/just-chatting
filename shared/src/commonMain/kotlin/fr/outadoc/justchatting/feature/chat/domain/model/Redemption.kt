package fr.outadoc.justchatting.feature.chat.domain.model

import kotlin.time.Instant

internal data class Redemption(
    val id: String,
    val userId: String,
    val userLogin: String,
    val userDisplayName: String,
    val userAddedMessage: String? = null,
    val redeemedAt: Instant? = null,
    val reward: Reward,
)
