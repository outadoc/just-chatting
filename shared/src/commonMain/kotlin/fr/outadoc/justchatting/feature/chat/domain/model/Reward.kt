package fr.outadoc.justchatting.feature.chat.domain.model

public data class Reward(
    val id: String,
    val title: String,
    val cost: Int,
    val backgroundColor: String? = null,
)
