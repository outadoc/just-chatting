package com.github.andreyasadchy.xtra.model.chat

class TwitchBadgesResponse(private val badges: Map<Pair<String, String>, TwitchBadge> = emptyMap()) {

    fun getTwitchBadge(name: String, version: String): TwitchBadge? = badges[Pair(name, version)]
}