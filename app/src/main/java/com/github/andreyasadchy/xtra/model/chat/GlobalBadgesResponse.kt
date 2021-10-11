package com.github.andreyasadchy.xtra.model.chat

class GlobalBadgesResponse(private val badges: Map<Pair<String, String>, GlobalBadge> = emptyMap()) {

    fun getGlobalBadge(name: String, version: String): GlobalBadge? = badges[Pair(name, version)]
}