package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.feature.irc.ChatMessageParser
import com.github.andreyasadchy.xtra.model.chat.Badge
import com.github.andreyasadchy.xtra.model.chat.ChatCommand
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PingCommand
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import kotlinx.datetime.Instant
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class ChatMessageParserTest {

    @Test
    fun `Parse simple PRIVMSG`() = test {
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
                isFirst = false,
                msgId = null,
                systemMsg = null,
                timestamp = Instant.parse("2017-10-05T23:36:12.675Z"),
                rewardId = null,
                pointReward = null
            )
        }
    }

    @Test
    fun `Parse action PRIVMSG`() = test {
        input { "@badge-info=subscriber/11;badges=moderator/1,subscriber/3009;color=#8A2BE2;display-name=pepitipepibot;emotes=;first-msg=0;flags=;id=7485e9c7-962f-4fe9-af27-3b625699f7e1;mod=1;returning-chatter=0;room-id=402890635;subscriber=1;tmi-sent-ts=1657298849487;turbo=0;user-id=651859616;user-type=mod :pepitipepibot!pepitipepibot@pepitipepibot.tmi.twitch.tv PRIVMSG #pelerine :\u0001ACTION ⭐️ RE-SUB ⭐️ Amphirae (+4) ⭐️\u0001" }
        expected {
            LiveChatMessage(
                id = "7485e9c7-962f-4fe9-af27-3b625699f7e1",
                userId = "651859616",
                userLogin = "pepitipepibot",
                userName = "pepitipepibot",
                message = "⭐️ RE-SUB ⭐️ Amphirae (+4) ⭐️",
                color = "#8A2BE2",
                isAction = true,
                emotes = emptyList(),
                badges = listOf(
                    Badge(id = "moderator", version = "1"),
                    Badge(id = "subscriber", version = "3009")
                ),
                isFirst = false,
                timestamp = Instant.parse("2022-07-08T16:47:29.487Z")
            )
        }
    }

    @Test
    fun `Parse sub gift USERNOTICE`() = test {
        input { "@badge-info=subscriber/41;badges=subscriber/36,bits/1000;color=#FFFFFF;display-name=Frfun;emotes=emotesv2_53f30305e78246aea4bc24d299dd09e7:0-5/emotesv2_f6bd60f5f3ef490aa4e40c7ee792c8c8:25-38;flags=;id=4c5a38ff-6bb3-4cad-a555-dc8a736cfc38;login=frfun;mod=0;msg-id=resub;msg-param-cumulative-months=41;msg-param-months=0;msg-param-multimonth-duration=0;msg-param-multimonth-tenure=0;msg-param-should-share-streak=1;msg-param-streak-months=40;msg-param-sub-plan-name=Channel\\sSubscription\\s(maghla);msg-param-sub-plan=Prime;msg-param-was-gifted=false;room-id=131215608;subscriber=1;system-msg=Frfun\\ssubscribed\\swith\\sPrime.\\sThey've\\ssubscribed\\sfor\\s41\\smonths,\\scurrently\\son\\sa\\s40\\smonth\\sstreak!;tmi-sent-ts=1657298959852;user-id=99037844;user-type= :tmi.twitch.tv USERNOTICE #maghla :coxPet pat pat le requin moumou4Content" }
        expected {
            Command.UserNotice(
                message = "Frfun subscribed with Prime. They've subscribed for 41 months, currently on a 40 month streak!",
                timestamp = Instant.parse("2022-07-08T16:49:19.852Z")
            )
        }
    }

    @Test
    fun `Parse announcement USERNOTICE`() = test {
        input { "@badge-info=subscriber/11;badges=moderator/1,subscriber/3009;color=#8A2BE2;display-name=pepitipepibot;emotes=;flags=;id=54b4d931-8db5-47ad-b6e7-6687cdbbb8ec;login=pepitipepibot;mod=1;msg-id=announcement;room-id=402890635;subscriber=1;system-msg=;tmi-sent-ts=1657301015335;user-id=651859616;user-type=mod :tmi.twitch.tv USERNOTICE #pelerine :LEZGONGUE LA MIXTAPE ELLE EST LAAAAAAAA : https://open.spotify.com/album/0X9kU5VLUmXoi6Hk6ou3PP?si=85JnJJSARpqCJ_ugsGNVhQ !! Pepe a 2 track : Dig dig deep deep & Light you up !" }
        expected {
            Command.UserNotice(
                message = "LEZGONGUE LA MIXTAPE ELLE EST LAAAAAAAA : https://open.spotify.com/album/0X9kU5VLUmXoi6Hk6ou3PP?si=85JnJJSARpqCJ_ugsGNVhQ !! Pepe a 2 track : Dig dig deep deep & Light you up !",
                timestamp = Instant.parse("2022-07-08T17:23:35.335Z")
            )
        }
    }

    @Test
    fun `Parse raid USERNOTICE`() = test {
        input { "@badge-info=;badges=hype-train/2;color=#C8C8C8;display-name=maxent__;emotes=;flags=;historical=1;id=5bca8513-e6b2-455b-a898-1cc7d3bf5332;login=maxent__;mod=0;msg-id=raid;msg-param-displayName=maxent__;msg-param-login=maxent__;msg-param-profileImageURL=https://static-cdn.jtvnw.net/jtv_user_pictures/12a5e085-db37-4e5d-b59a-77eb9f6dd8a2-profile_image-70x70.png;msg-param-viewerCount=3;rm-received-ts=1657303912918;room-id=402890635;subscriber=0;system-msg=3\\sraiders\\sfrom\\smaxent__\\shave\\sjoined!;tmi-sent-ts=1657303912832;user-id=563254735;user-type= :tmi.twitch.tv USERNOTICE #pelerine\n" }
        expected {
            Command.UserNotice(
                message = "3 raiders from maxent__ have joined!",
                timestamp = Instant.parse("2022-07-08T18:11:52.832Z")
            )
        }
    }

    @Test
    fun `Parse PING`() = test {
        input { "PING :tmi.twitch.tv" }
        expected { PingCommand }
    }

    @Test
    fun `Parse simple ROOMSTATE`() = test {
        input { "@emote-only=0;followers-only=-1;r9k=0;room-id=402890635;slow=0;subs-only=0 :tmi.twitch.tv ROOMSTATE #pelerine" }
        expected {
            RoomState(
                isEmoteOnly = false,
                minFollowDuration = null,
                slowModeDuration = null,
                uniqueMessagesOnly = false,
                isSubOnly = false
            )
        }
    }

    @Test
    fun `Parse custom ROOMSTATE`() = test {
        input { "@emote-only=1;followers-only=15;r9k=1;room-id=402890635;slow=120;subs-only=1 :tmi.twitch.tv ROOMSTATE #pelerine" }
        expected {
            RoomState(
                isEmoteOnly = true,
                minFollowDuration = 15.minutes,
                slowModeDuration = 2.minutes,
                uniqueMessagesOnly = true,
                isSubOnly = true
            )
        }
    }

    private lateinit var parser: ChatMessageParser

    @Before
    fun before() {
        parser = ChatMessageParser()
    }

    data class Assertion(val input: String, val expected: ChatCommand) {
        class Builder {
            private var input: String? = null
            private var expected: ChatCommand? = null

            fun input(block: () -> String) {
                input = block()
            }

            fun expected(block: () -> ChatCommand) {
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