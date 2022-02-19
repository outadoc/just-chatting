package com.github.andreyasadchy.xtra.model.helix.follows

enum class Sort(val value: String) {
    FOLLOWED_AT("created_at"),
    ALPHABETICALLY("login"),
    LAST_BROADCAST("last_broadcast");

    override fun toString() = value
}