package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.Badge
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import kotlinx.datetime.Instant
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ChatMessageParserTest {

    @Test
    fun `Parse a simple chat message`() = test {
        input { "@badge-info=;badges=turbo/1;color=#0D4200;display-name=ronni;emotes=25:0-4,12-16/1902:6-10;id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;mod=0;room-id=1337;subscriber=0;tmi-sent-ts=1507246572675;turbo=1;user-id=1337;user-type=global_mod :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa Keepo Kappa" }
        expected {
            LiveChatMessage(
                id = "b34ccfc7-4977-403a-8a94-33c6bac34fb8",
                userId = "1337",
                userLogin = "ronni",
                userName = "ronni",
                message = "Kappa Keepo Kappa",
                color = "#0D4200",
                isAction = false,
                emotes = listOf(
                    TwitchEmote(
                        name = "25",
                        id = "25",
                        begin = 0,
                        end = 4,
                        setId = null,
                        ownerId = null,
                        supportedFormats = listOf(
                            "default"
                        ),
                        supportedScales = mapOf(1.0f to "1.0", 2.0f to "2.0", 3.0f to "3.0"),
                        supportedThemes = listOf("dark")
                    ),
                    TwitchEmote(
                        name = "25",
                        id = "25",
                        begin = 12,
                        end = 16,
                        setId = null,
                        ownerId = null,
                        supportedFormats = listOf(
                            "default"
                        ),
                        supportedScales = mapOf(1.0f to "1.0", 2.0f to "2.0", 3.0f to "3.0"),
                        supportedThemes = listOf("dark")
                    ),
                    TwitchEmote(
                        name = "1902",
                        id = "1902",
                        begin = 6,
                        end = 10,
                        setId = null,
                        ownerId = null,
                        supportedFormats = listOf(
                            "default"
                        ),
                        supportedScales = mapOf(1.0f to "1.0", 2.0f to "2.0", 3.0f to "3.0"),
                        supportedThemes = listOf("dark")
                    )
                ),
                badges = listOf(
                    Badge(id = "turbo", version = "1")
                ),
                fullMsg = "@badge-info=;badges=turbo/1;color=#0D4200;display-name=ronni;emotes=25:0-4,12-16/1902:6-10;id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;mod=0;room-id=1337;subscriber=0;tmi-sent-ts=1507246572675;turbo=1;user-id=1337;user-type=global_mod :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa Keepo Kappa",
                isFirst = false,
                msgId = null,
                systemMsg = null,
                timestamp = Instant.parse("2017-10-05T23:36:12.675Z"),
                rewardId = null,
                pointReward = null
            )
        }
    }

    private lateinit var parser: ChatMessageParser

    @Before
    fun before() {
        parser = ChatMessageParser()
    }

    data class Assertion(val input: String, val expected: ChatMessage) {
        class Builder {
            private var input: String? = null
            private var expected: ChatMessage? = null

            fun input(block: () -> String) {
                input = block()
            }

            fun expected(block: () -> ChatMessage) {
                expected = block()
            }

            fun build() = Assertion(input!!, expected!!)
        }
    }

    private fun test(block: Assertion.Builder.() -> Unit) {
        val assertion = Assertion.Builder().apply(block).build()
        Assert.assertEquals(
            assertion.expected,
            parser.parse(assertion.input)
        )
    }
}