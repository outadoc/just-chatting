package com.github.andreyasadchy.xtra.model.helix.follows

enum class Order(val value: String) {
    ASC("asc"),
    DESC("desc");

    override fun toString() = value
}