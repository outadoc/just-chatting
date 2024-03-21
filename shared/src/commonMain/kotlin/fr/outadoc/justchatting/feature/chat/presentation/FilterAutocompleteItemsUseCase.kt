package fr.outadoc.justchatting.feature.chat.presentation

import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import com.willowtreeapps.fuzzywuzzy.diffutils.model.ExtractedResult
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Emote
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentSet

class FilterAutocompleteItemsUseCase {

    operator fun invoke(
        filter: CharSequence,
        recentEmotes: List<Emote>,
        allEmotesMap: ImmutableMap<String, Emote>,
        chatters: PersistentSet<Chatter>,
    ): List<AutoCompleteItem> {
        val cleanFilter: CharSequence = filter
            .removePrefix(ChatPrefixConstants.ChatterPrefix.toString())
            .removePrefix(ChatPrefixConstants.EmotePrefix.toString())

        val prefix: Char? = filter
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
            }
        }

        val emoteItems: List<AutoCompleteItem.Emote> =
            if (prefix == null || prefix == ChatPrefixConstants.EmotePrefix) {
                FuzzySearch.extractTop(
                    query = cleanFilter.toString(),
                    choices = allEmotesMap.keys.toList(),
                    limit = 10,
                ).mapNotNull { res: ExtractedResult ->
                    val emoteName = res.string
                    allEmotesMap[emoteName]?.let { emote ->
                        AutoCompleteItem.Emote(emote)
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
