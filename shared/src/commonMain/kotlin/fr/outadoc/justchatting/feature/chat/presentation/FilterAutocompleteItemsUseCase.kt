package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

internal class FilterAutocompleteItemsUseCase {
    operator fun invoke(
        filter: CharSequence,
        recentEmotes: List<Emote>,
        allEmotesMap: ImmutableMap<String, Emote>,
        chatters: PersistentSet<Chatter>,
    ): ImmutableList<AutoCompleteItem> {
        val cleanFilter: CharSequence =
            filter
                .removePrefix(ChatPrefixConstants.ChatterPrefix.toString())
                .removePrefix(ChatPrefixConstants.EmotePrefix.toString())

        val prefix: Char? =
            filter
                .firstOrNull()
                ?.takeIf { filter != cleanFilter }

        if (cleanFilter.isBlank()) {
            return when (prefix) {
                ChatPrefixConstants.ChatterPrefix -> {
                    chatters.map { chatter ->
                        AutoCompleteItem.User(chatter)
                    }
                }

                ChatPrefixConstants.EmotePrefix -> {
                    allEmotesMap.map { emote ->
                        AutoCompleteItem.Emote(emote.value)
                    }
                }

                else -> {
                    recentEmotes.map { emote ->
                        AutoCompleteItem.Emote(emote)
                    }
                }
            }.toImmutableList()
        }

        val emoteItems: PersistentList<AutoCompleteItem.Emote> =
            if (prefix == null || prefix == ChatPrefixConstants.EmotePrefix) {
                allEmotesMap
                    .mapNotNull { emote ->
                        if (emote.key.contains(cleanFilter, ignoreCase = true)) {
                            AutoCompleteItem.Emote(emote.value)
                        } else {
                            null
                        }
                    }.toPersistentList()
            } else {
                persistentListOf()
            }

        val chatterItems: PersistentList<AutoCompleteItem.User> =
            if (prefix == null || prefix == ChatPrefixConstants.ChatterPrefix) {
                chatters
                    .mapNotNull { chatter ->
                        if (chatter.contains(cleanFilter)) {
                            AutoCompleteItem.User(chatter)
                        } else {
                            null
                        }
                    }.toPersistentList()
            } else {
                persistentListOf()
            }

        return emoteItems + chatterItems
    }
}
