package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.utils.presentation.isValidWebUrl
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote as EmoteModel

/**
 * A piece of a chat message, as it should be displayed. Each platform decides how to draw each
 * kind of token, but which words are links, emotes or mentions is decided here, once.
 */
public sealed interface MessageToken {
    /** The words this token stands for, as they were typed. */
    public val text: String

    public data class Word(
        override val text: String,
    ) : MessageToken

    public data class Link(
        override val text: String,
        val url: String,
    ) : MessageToken

    public data class Mention(
        override val text: String,
        val isMentionOfAppUser: Boolean,
    ) : MessageToken

    /**
     * An emote or a cheer, and the zero-width emotes that are drawn on top of it, if any.
     */
    public data class Emote(
        override val text: String,
        val emote: EmoteModel,
        val overlays: List<EmoteModel>,
    ) : MessageToken
}

/**
 * Splits this message into tokens.
 *
 * @param emotes the emotes that can be used in this chat, by name.
 * @param cheerEmotes the cheers that can be used in this chat, by name.
 * @param appUserLogin the login of the user of the app, to highlight the messages mentioning them.
 */
public fun ChatListItem.Message.Body.tokenize(
    emotes: Map<String, EmoteModel>,
    cheerEmotes: Map<String, EmoteModel>,
    appUserLogin: String,
): List<MessageToken> {
    val words = message?.split(' ')?.filter { word -> word.isNotEmpty() } ?: return emptyList()
    val embeddedEmotes = embeddedEmotes.associateBy { emote -> emote.name }

    // Emotes sent with the message take precedence, then cheers, then the chat's emotes.
    fun emoteNamed(word: String): EmoteModel? = embeddedEmotes[word] ?: cheerEmotes[word] ?: emotes[word]

    val tokens = mutableListOf<MessageToken>()
    var index = 0

    while (index < words.size) {
        val word = words[index]
        val emote = emoteNamed(word)

        when {
            word.isValidWebUrl() -> {
                val url = if (word.startsWith("http")) word else "https://$word"
                tokens.add(MessageToken.Link(text = word, url = url))
                index++
            }

            emote != null -> {
                // Zero-width emotes that follow a regular emote are drawn on top of it
                val overlays =
                    if (emote.isZeroWidth) {
                        emptyList()
                    } else {
                        words
                            .drop(index + 1)
                            .asSequence()
                            .map { next -> embeddedEmotes[next] ?: emotes[next] }
                            .takeWhile { next -> next != null && next.isZeroWidth }
                            .filterNotNull()
                            .toList()
                    }

                val groupSize = 1 + overlays.size
                tokens.add(
                    MessageToken.Emote(
                        text = words.subList(index, index + groupSize).joinToString(separator = " "),
                        emote = emote,
                        overlays = overlays,
                    ),
                )
                index += groupSize
            }

            word.startsWith(ChatPrefixConstants.ChatterPrefix) -> {
                tokens.add(
                    MessageToken.Mention(
                        text = word,
                        isMentionOfAppUser = isMentionOf(mention = word, login = appUserLogin),
                    ),
                )
                index++
            }

            else -> {
                tokens.add(MessageToken.Word(text = word))
                index++
            }
        }
    }

    return tokens
}

/**
 * Whether [mention], a word from a chat message such as `@login` or `@login,`, mentions the user
 * with the given [login]. The mention prefix and any trailing punctuation are ignored.
 */
public fun isMentionOf(
    mention: String,
    login: String,
): Boolean {
    val mentionedName =
        mention
            .removePrefix(ChatPrefixConstants.ChatterPrefix.toString())
            .trimEnd { char -> !char.isLetterOrDigit() && char != '_' }

    return mentionedName.isNotEmpty() && mentionedName.equals(login, ignoreCase = true)
}
