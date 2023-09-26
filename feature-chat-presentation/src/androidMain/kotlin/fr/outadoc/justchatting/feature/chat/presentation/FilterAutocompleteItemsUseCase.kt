package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Emote
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentSet

class FilterAutocompleteItemsUseCase {

    operator fun invoke(
        filter: CharSequence,
        allEmotesMap: ImmutableMap<String, Emote>,
        chatters: PersistentSet<Chatter>,
    ): List<AutoCompleteItem> {
        val cleanFilter: CharSequence = filter
            .removePrefix(ChatPrefixConstants.ChatterPrefix.toString())
            .removePrefix(ChatPrefixConstants.EmotePrefix.toString())

        if (cleanFilter.isBlank()) {
            // If nothing has been typed, just show all emotes!
            return allEmotesMap.mapNotNull { emote ->
                AutoCompleteItem.Emote(emote.value)
            }
        }

        val prefix: Char? = filter
            .firstOrNull()
            ?.takeIf { filter != cleanFilter }

        val emoteItems: List<AutoCompleteItem.Emote> =
            if (prefix == null || prefix == ChatPrefixConstants.EmotePrefix) {
                allEmotesMap.mapNotNull { emote ->
                    if (emote.key.contains(cleanFilter, ignoreCase = true)) {
                        AutoCompleteItem.Emote(emote.value)
                    } else {
                        null
                    }
                }
            } else {
                emptyList()
            }

        val chatterItems: List<AutoCompleteItem.User> =
            if (prefix == null || prefix == ChatPrefixConstants.ChatterPrefix) {
                chatters.mapNotNull { chatter ->
                    if (chatter.contains(cleanFilter)) {
                        AutoCompleteItem.User(chatter)
                    } else {
                        null
                    }
                }
            } else {
                emptyList()
            }

        return emoteItems + chatterItems
    }
}
