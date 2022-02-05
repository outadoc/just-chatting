package com.github.andreyasadchy.xtra.model.chat

abstract class Emote {
    abstract val name: String
    abstract val url: String //TODO null if property
    open val type: String
        get() = "image/png"
    open val zeroWidth: Boolean
        get() = false
    open val ownerId: String?
        get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Emote || name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return ":$name"
    }
}