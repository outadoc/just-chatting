package fr.outadoc.justchatting.feature.chat.domain.model

import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.datetime.Instant

internal data class Redemption(
    val id: String,
    val user: User,
    val userAddedMessage: String? = null,
    val redeemedAt: Instant? = null,
    val reward: Reward,
)
