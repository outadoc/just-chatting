package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

// Lives in desktopTest because link detection is platform-specific (java.net.URL here).
internal class MessageTokenTest {
    private fun emote(
        name: String,
        isZeroWidth: Boolean = false,
    ) = Emote(
        name = name,
        urls = EmoteUrls(url = "https://example.com/$name.png"),
        isZeroWidth = isZeroWidth,
    )

    private val kappa = emote("Kappa")
    private val pog = emote("Pog")
    private val rain = emote("RainTime", isZeroWidth = true)
    private val snow = emote("SnowTime", isZeroWidth = true)
    private val cheer = emote("Cheer100")

    private val emotes = listOf(kappa, pog, rain, snow).associateBy { it.name }

    private fun body(
        message: String?,
        embeddedEmotes: List<Emote> = emptyList(),
    ) = ChatListItem.Message.Body(
        messageId = "id",
        message = message,
        chatter = Chatter(id = "1", login = "chatter", displayName = "Chatter"),
        embeddedEmotes = persistentListOf(*embeddedEmotes.toTypedArray()),
    )

    private fun tokenize(
        message: String?,
        embeddedEmotes: List<Emote> = emptyList(),
    ) = body(message, embeddedEmotes).tokenize(
        emotes = emotes,
        cheerEmotes = mapOf(cheer.name to cheer),
        appUserLogin = "outadoc",
    )

    @Test
    fun `message without text has no tokens`() {
        assertEquals(emptyList(), tokenize(null))
    }

    @Test
    fun `plain words are kept as words`() {
        assertEquals(
            listOf(MessageToken.Word("hello"), MessageToken.Word("world")),
            tokenize("hello world"),
        )
    }

    @Test
    fun `repeated spaces are ignored`() {
        assertEquals(
            listOf(MessageToken.Word("hello"), MessageToken.Word("world")),
            tokenize("hello   world"),
        )
    }

    @Test
    fun `web links are detected`() {
        assertEquals(
            listOf(
                MessageToken.Word("see"),
                MessageToken.Link(text = "https://example.com/a", url = "https://example.com/a"),
            ),
            tokenize("see https://example.com/a"),
        )
    }

    @Test
    fun `chat emotes and cheers are detected`() {
        assertEquals(
            listOf(
                MessageToken.Emote(text = "Kappa", emote = kappa, overlays = emptyList()),
                MessageToken.Word("hi"),
                MessageToken.Emote(text = "Cheer100", emote = cheer, overlays = emptyList()),
            ),
            tokenize("Kappa hi Cheer100"),
        )
    }

    @Test
    fun `emotes sent with the message take precedence over chat emotes`() {
        val embeddedKappa = emote("Kappa").copy(ownerId = "twitch")

        assertEquals(
            listOf(MessageToken.Emote(text = "Kappa", emote = embeddedKappa, overlays = emptyList())),
            tokenize("Kappa", embeddedEmotes = listOf(embeddedKappa)),
        )
    }

    @Test
    fun `zero-width emotes are grouped with the emote before them`() {
        assertEquals(
            listOf(
                MessageToken.Emote(text = "Pog RainTime SnowTime", emote = pog, overlays = listOf(rain, snow)),
                MessageToken.Word("wow"),
            ),
            tokenize("Pog RainTime SnowTime wow"),
        )
    }

    @Test
    fun `zero-width emote on its own is not an overlay`() {
        assertEquals(
            listOf(
                MessageToken.Emote(text = "RainTime", emote = rain, overlays = emptyList()),
                MessageToken.Emote(text = "SnowTime", emote = snow, overlays = emptyList()),
            ),
            tokenize("RainTime SnowTime"),
        )
    }

    @Test
    fun `mentions of the app user are flagged`() {
        assertEquals(
            listOf(
                MessageToken.Mention(text = "@OutaDoc,", isMentionOfAppUser = true),
                MessageToken.Mention(text = "@someone", isMentionOfAppUser = false),
            ),
            tokenize("@OutaDoc, @someone"),
        )
    }
}
