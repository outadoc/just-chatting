package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterAutocompleteItemsUseCaseTest {

    private val instance = FilterAutocompleteItemsUseCase()

    private val mockEmotes = persistentMapOf(
        "ḧtyLaser" to Emote(name = "htyLaser", urls = EmoteUrls("")),
        "ponceFAB" to Emote(name = "ponceFAB", urls = EmoteUrls("")),
        "bgrLoutre" to Emote(name = "bgrLoutre", urls = EmoteUrls("")),
    )

    private val mockChatters = persistentSetOf(
        Chatter(id = "1", login = "ultia", displayName = "Ultia"),
        Chatter(id = "2", login = "ponce", displayName = "Ponce"),
        Chatter(id = "3", login = "rivenzi", displayName = "Rivenzi"),
    )

    @Test
    fun `When parsing an empty string, nothing is returned`() {
        assertEquals(
            emptyList<AutoCompleteItem>(),
            instance(filter = "", mockEmotes, mockChatters),
        )
    }

    @Test
    fun `When parsing a blank string, nothing is returned`() {
        assertEquals(
            emptyList<AutoCompleteItem>(),
            instance(filter = "   ", mockEmotes, mockChatters),
        )
    }

    @Test
    fun `When parsing a string with only a prefix, nothing is returned`() {
        assertEquals(
            emptyList<AutoCompleteItem>(),
            instance(filter = ":", mockEmotes, mockChatters),
        )

        assertEquals(
            emptyList<AutoCompleteItem>(),
            instance(filter = "@", mockEmotes, mockChatters),
        )
    }

    @Test
    fun `When parsing a string matching both a chatter and an emote, both are returned`() {
        assertEquals(
            listOf(
                AutoCompleteItem.Emote(Emote(name = "ponceFAB", urls = EmoteUrls(""))),
                AutoCompleteItem.User(Chatter(id = "2", login = "ponce", displayName = "Ponce")),
            ),
            instance(filter = "ponce", mockEmotes, mockChatters),
        )
    }

    @Test
    fun `When parsing a string matching both a chatter and an emote, with a emote prefix, only the emote is returned`() {
        assertEquals(
            listOf(
                AutoCompleteItem.Emote(Emote(name = "ponceFAB", urls = EmoteUrls(""))),
            ),
            instance(filter = ":ponce", mockEmotes, mockChatters),
        )
    }

    @Test
    fun `When parsing a string matching both a chatter and an emote, with a chatter prefix, only the chatter is returned`() {
        assertEquals(
            listOf(
                AutoCompleteItem.User(Chatter(id = "2", login = "ponce", displayName = "Ponce")),
            ),
            instance(filter = "@ponce", mockEmotes, mockChatters),
        )
    }

    @Test
    fun `When parsing a string matching multiple items, they are all returned`() {
        assertEquals(
            listOf(
                AutoCompleteItem.Emote(Emote(name = "htyLaser", urls = EmoteUrls(""))),
                AutoCompleteItem.Emote(Emote(name = "ponceFAB", urls = EmoteUrls(""))),
                AutoCompleteItem.User(Chatter(id = "1", login = "ultia", displayName = "Ultia")),
            ),
            instance(filter = "a", mockEmotes, mockChatters),
        )
    }
}
