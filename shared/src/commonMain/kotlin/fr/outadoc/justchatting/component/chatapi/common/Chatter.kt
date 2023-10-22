package fr.outadoc.justchatting.component.chatapi.common

data class Chatter(
    val id: String,
    val login: String,
    val displayName: String,
) {
    fun contains(word: CharSequence): Boolean =
        displayName.contains(word, ignoreCase = true) ||
            login.contains(word, ignoreCase = true)

    fun matches(word: CharSequence): Boolean =
        displayName.contentEquals(word, ignoreCase = true) ||
            login.contentEquals(word, ignoreCase = true)

    /**
     * Checks whether the chatter's display name is just their login but with
     * different capitalization, or actually a different string (usually in a different alphabet).
     *
     * @return true if the display name is noticeably different from the login.
     */
    val hasLocalizedDisplayName: Boolean =
        !login.contentEquals(displayName, ignoreCase = true)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Chatter

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
