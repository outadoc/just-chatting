package fr.outadoc.justchatting.component.ircparser.core.message

data class IrcMessageComponents(
    val parameters: List<String> = listOf(),
    val tags: Map<String, String?> = mapOf(),
    val prefix: String? = null,
)
