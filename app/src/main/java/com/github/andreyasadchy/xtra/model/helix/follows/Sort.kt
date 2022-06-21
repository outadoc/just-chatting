package com.github.andreyasadchy.xtra.model.helix.follows

enum class Sort(val value: String) {
    FOLLOWED_AT("created_at"),
    ALPHABETICALLY("login");

    override fun toString() = value
}
