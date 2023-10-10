package fr.outadoc.justchatting.component.ircparser.core.message

data class IrcMessage(
    val tags: Map<String, String?> = emptyMap(),
    val prefix: String? = null,
    val command: String,
    val parameters: List<String> = emptyList(),
) {
    override fun toString(): String {
        val pieces = mutableListOf<String>()

        if (tags.isNotEmpty()) {
            pieces += "tags=$tags"
        }

        if (!prefix.isNullOrEmpty()) {
            pieces += "prefix=$prefix"
        }

        pieces += "command=$command"

        if (parameters.isNotEmpty()) {
            pieces += "parameters=$parameters"
        }

        return "IrcMessage(${pieces.joinToString()})"
    }
}
