package fr.outadoc.justchatting.model.chat

abstract class Emote {

    abstract val name: String

    open val ownerId: String? get() = null
    open val isZeroWidth: Boolean get() = false

    abstract fun getUrl(
        animate: Boolean,
        screenDensity: Float = 1f,
        isDarkTheme: Boolean = false
    ): String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Emote || name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
