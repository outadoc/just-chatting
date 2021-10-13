package com.github.andreyasadchy.xtra.model.chat

class GlobalBadgesResponse(private val badges: Map<Pair<String, String>, TwitchBadge> = emptyMap()) {

    fun getGlobalBadge(name: String, version: String): TwitchBadge? = badges[Pair(name, version)]
}