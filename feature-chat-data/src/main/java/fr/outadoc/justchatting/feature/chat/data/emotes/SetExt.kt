package fr.outadoc.justchatting.feature.chat.data.emotes

internal fun <T> flatListOf(head: T, vararg lists: List<T>): List<T> {
    val content = lists.flatMap { item -> item }
    return if (content.isNotEmpty()) listOf(head) + content else emptyList()
}
