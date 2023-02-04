package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.ChatEmote
import fr.outadoc.justchatting.component.twitch.utils.map
import fr.outadoc.justchatting.component.twitch.websocket.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.Message
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.PingCommand
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.RoomStateDelta
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.UserState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TwitchIrcCommandParserTest {

    @Test
    fun `Parse simple PRIVMSG`() = test {
        input { "@badge-info=;badges=turbo/1;color=#0D4200;display-name=ronni;emotes=25:0-4,12-16/1902:6-10;id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;mod=0;room-id=1337;subscriber=0;tmi-sent-ts=1507246572675;turbo=1;user-id=1337;user-type=global_mod :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa Keepo Kappa" }
        expected {
            Message.ChatMessage(
                id = "b34ccfc7-4977-403a-8a94-33c6bac34fb8",
                userId = "1337",
                userLogin = "ronni",
                userName = "ronni",
                message = "Kappa Keepo Kappa",
                color = "#0D4200",
                isAction = false,
                embeddedEmotes = listOf(
                    ChatEmote(
                        id = "25",
                        name = "Kappa",
                    ).map(),
                    ChatEmote(
                        id = "25",
                        name = "Kappa",
                    ).map(),
                    ChatEmote(
                        id = "1902",
                        name = "Keepo",
                    ).map(),
                ),
                badges = listOf(
                    Badge(
                        id = "turbo",
                        version = "1",
                    ),
                ),
                isFirst = false,
                timestamp = Instant.parse("2017-10-05T23:36:12.675Z"),
                systemMsg = null,
                rewardId = null,
                inReplyTo = null,
                msgId = null,
            )
        }
    }

    @Test
    fun `Parse action PRIVMSG`() = test {
        input { "@badge-info=subscriber/11;badges=moderator/1,subscriber/3009;color=#8A2BE2;display-name=pepitipepibot;emotes=;first-msg=0;flags=;id=7485e9c7-962f-4fe9-af27-3b625699f7e1;mod=1;returning-chatter=0;room-id=402890635;subscriber=1;tmi-sent-ts=1657298849487;turbo=0;user-id=651859616;user-type=mod :pepitipepibot!pepitipepibot@pepitipepibot.tmi.twitch.tv PRIVMSG #pelerine :\u0001ACTION ⭐️ RE-SUB ⭐️ Amphirae (+4) ⭐️\u0001" }
        expected {
            Message.ChatMessage(
                id = "7485e9c7-962f-4fe9-af27-3b625699f7e1",
                userId = "651859616",
                userLogin = "pepitipepibot",
                userName = "pepitipepibot",
                message = "⭐️ RE-SUB ⭐️ Amphirae (+4) ⭐️",
                color = "#8A2BE2",
                isAction = true,
                embeddedEmotes = emptyList(),
                badges = listOf(
                    Badge(
                        id = "moderator",
                        version = "1",
                    ),
                    Badge(
                        id = "subscriber",
                        version = "3009",
                    ),
                ),
                isFirst = false,
                timestamp = Instant.parse("2022-07-08T16:47:29.487Z"),
                systemMsg = null,
                rewardId = null,
                inReplyTo = null,
                msgId = null,
            )
        }
    }

    @Test
    fun `Parse sub gift USERNOTICE`() = test {
        input { "@badge-info=subscriber/41;badges=subscriber/36,bits/1000;color=#FFFFFF;display-name=Frfun;emotes=emotesv2_53f30305e78246aea4bc24d299dd09e7:0-5/emotesv2_f6bd60f5f3ef490aa4e40c7ee792c8c8:25-38;flags=;id=4c5a38ff-6bb3-4cad-a555-dc8a736cfc38;login=frfun;mod=0;msg-id=resub;msg-param-cumulative-months=41;msg-param-months=0;msg-param-multimonth-duration=0;msg-param-multimonth-tenure=0;msg-param-should-share-streak=1;msg-param-streak-months=40;msg-param-sub-plan-name=Channel\\sSubscription\\s(maghla);msg-param-sub-plan=Prime;msg-param-was-gifted=false;room-id=131215608;subscriber=1;system-msg=Frfun\\ssubscribed\\swith\\sPrime.\\sThey've\\ssubscribed\\sfor\\s41\\smonths,\\scurrently\\son\\sa\\s40\\smonth\\sstreak!;tmi-sent-ts=1657298959852;user-id=99037844;user-type= :tmi.twitch.tv USERNOTICE #maghla :coxPet pat pat le requin moumou4Content" }
        expected {
            Message.UserNotice(
                systemMsg = "Frfun subscribed with Prime. They've subscribed for 41 months, currently on a 40 month streak!",
                timestamp = Instant.parse("2022-07-08T16:49:19.852Z"),
                msgId = "resub",
                userMessage = Message.ChatMessage(
                    id = "4c5a38ff-6bb3-4cad-a555-dc8a736cfc38",
                    userId = "99037844",
                    userLogin = "frfun",
                    userName = "Frfun",
                    message = "coxPet pat pat le requin moumou4Content",
                    color = "#FFFFFF",
                    systemMsg = "Frfun subscribed with Prime. They've subscribed for 41 months, currently on a 40 month streak!",
                    isAction = false,
                    embeddedEmotes = listOf(
                        ChatEmote(
                            id = "emotesv2_53f30305e78246aea4bc24d299dd09e7",
                            name = "coxPet",
                        ).map(),
                        ChatEmote(
                            id = "emotesv2_f6bd60f5f3ef490aa4e40c7ee792c8c8",
                            name = "moumou4Content",
                        ).map(),
                    ),
                    badges = listOf(
                        Badge(
                            id = "subscriber",
                            version = "36",
                        ),
                        Badge(
                            id = "bits",
                            version = "1000",
                        ),
                    ),
                    isFirst = false,
                    timestamp = Instant.parse("2022-07-08T16:49:19.852Z"),
                    rewardId = null,
                    msgId = "resub",
                    inReplyTo = null,
                ),
            )
        }
    }

    @Test
    fun `Parse announcement USERNOTICE`() = test {
        input { "@badge-info=subscriber/11;badges=moderator/1,subscriber/3009;color=#8A2BE2;display-name=pepitipepibot;emotes=;flags=;id=54b4d931-8db5-47ad-b6e7-6687cdbbb8ec;login=pepitipepibot;mod=1;msg-id=announcement;room-id=402890635;subscriber=1;system-msg=;tmi-sent-ts=1657301015335;user-id=651859616;user-type=mod :tmi.twitch.tv USERNOTICE #pelerine :LEZGONGUE LA MIXTAPE ELLE EST LAAAAAAAA : https://open.spotify.com/album/0X9kU5VLUmXoi6Hk6ou3PP?si=85JnJJSARpqCJ_ugsGNVhQ !! Pepe a 2 track : Dig dig deep deep & Light you up !" }
        expected {
            Message.UserNotice(
                timestamp = Instant.parse("2022-07-08T17:23:35.335Z"),
                msgId = "announcement",
                userMessage = Message.ChatMessage(
                    id = "54b4d931-8db5-47ad-b6e7-6687cdbbb8ec",
                    userId = "651859616",
                    userLogin = "pepitipepibot",
                    userName = "pepitipepibot",
                    color = "#8A2BE2",
                    embeddedEmotes = emptyList(),
                    badges = listOf(
                        Badge(
                            id = "moderator",
                            version = "1",
                        ),
                        Badge(
                            id = "subscriber",
                            version = "3009",
                        ),
                    ),
                    timestamp = Instant.parse("2022-07-08T17:23:35.335Z"),
                    message = "LEZGONGUE LA MIXTAPE ELLE EST LAAAAAAAA : https://open.spotify.com/album/0X9kU5VLUmXoi6Hk6ou3PP?si=85JnJJSARpqCJ_ugsGNVhQ !! Pepe a 2 track : Dig dig deep deep & Light you up !",
                    msgId = "announcement",
                    systemMsg = null,
                    rewardId = null,
                    inReplyTo = null,
                ),
                systemMsg = null,
            )
        }
    }

    @Test
    fun `Parse raid USERNOTICE`() = test {
        input { "@badge-info=;badges=hype-train/2;color=#C8C8C8;display-name=maxent__;emotes=;flags=;historical=1;id=5bca8513-e6b2-455b-a898-1cc7d3bf5332;login=maxent__;mod=0;msg-id=raid;msg-param-displayName=maxent__;msg-param-login=maxent__;msg-param-profileImageURL=https://static-cdn.jtvnw.net/jtv_user_pictures/12a5e085-db37-4e5d-b59a-77eb9f6dd8a2-profile_image-70x70.png;msg-param-viewerCount=3;rm-received-ts=1657303912918;room-id=402890635;subscriber=0;system-msg=3\\sraiders\\sfrom\\smaxent__\\shave\\sjoined!;tmi-sent-ts=1657303912832;user-id=563254735;user-type= :tmi.twitch.tv USERNOTICE #pelerine\n" }
        expected {
            Message.UserNotice(
                systemMsg = "3 raiders from maxent__ have joined!",
                timestamp = Instant.parse("2022-07-08T18:11:52.832Z"),
                msgId = "raid",
                userMessage = null,
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
            RoomStateDelta(
                isEmoteOnly = false,
                minFollowDuration = null,
                slowModeDuration = null,
                uniqueMessagesOnly = false,
                isSubOnly = false,
            )
        }
    }

    @Test
    fun `Parse custom ROOMSTATE`() = test {
        input { "@emote-only=1;followers-only=15;r9k=1;room-id=402890635;slow=120;subs-only=1 :tmi.twitch.tv ROOMSTATE #pelerine" }
        expected {
            RoomStateDelta(
                isEmoteOnly = true,
                minFollowDuration = 15.minutes,
                slowModeDuration = 2.minutes,
                uniqueMessagesOnly = true,
                isSubOnly = true,
            )
        }
    }

    @Test
    fun `Parse CLEARCHAT permanent ban message`() = test {
        input { "@room-id=12345678;target-user-id=87654321;tmi-sent-ts=1642715756806 :tmi.twitch.tv CLEARCHAT #dallas :ronni" }
        expected {
            Message.ClearChat(
                timestamp = Instant.parse("2022-01-20T21:55:56.806Z"),
                userId = "87654321",
                userLogin = "ronni",
                duration = null,
            )
        }
    }

    @Test
    fun `Parse CLEARCHAT temporary ban message`() = test {
        input { "@ban-duration=350;room-id=12345678;target-user-id=87654321;tmi-sent-ts=1642719320727 :tmi.twitch.tv CLEARCHAT #dallas :ronni" }
        expected {
            Message.ClearChat(
                timestamp = Instant.parse("2022-01-20T22:55:20.727Z"),
                userLogin = "ronni",
                userId = "87654321",
                duration = 350.seconds,
            )
        }
    }

    @Test
    fun `Parse CLEARCHAT global clear message`() = test {
        input { "@room-id=12345678;tmi-sent-ts=1642715695392 :tmi.twitch.tv CLEARCHAT #dallas" }
        expected {
            Message.ClearChat(
                timestamp = Instant.parse("2022-01-20T21:54:55.392Z"),
                userId = null,
                userLogin = null,
                duration = null,
            )
        }
    }

    @Test
    fun `Parse USERSTATE message`() = test {
        input { "@badge-info=;badges=;color=#F85E10;display-name=outadoc;emote-sets=0,19194,553791,773027,300374282,380519965,477339272,494046698,537206155,592920959,610186276;id=e39d4a41-a7c8-4a24-845d-3e1c26aaf63f;mod=0;subscriber=0;user-type= :tmi.twitch.tv USERSTATE #mistermv" }
        expected {
            UserState(
                emoteSets = listOf(
                    "0",
                    "19194",
                    "553791",
                    "773027",
                    "300374282",
                    "380519965",
                    "477339272",
                    "494046698",
                    "537206155",
                    "592920959",
                    "610186276",
                ),
            )
        }
    }

    @Test
    fun `Parse PRIVMSG replying to another`() = test {
        input { "@badge-info=;badges=;client-nonce=8ffba7725e1a778b62f355ec990599ce;color=#B22222;display-name=tpanax;emotes=;first-msg=0;flags=;id=99d64122-2006-4e46-a43f-2b43f0ff9341;mod=0;reply-parent-display-name=Brankhorst;reply-parent-msg-body=On\\sdirait\\sEVE\\son\\sline;reply-parent-msg-id=7ffcf399-8d69-495c-920c-ea15a96eeee4;reply-parent-user-id=108193474;reply-parent-user-login=brankhorst;returning-chatter=0;room-id=28575692;subscriber=0;tmi-sent-ts=1661281318667;turbo=0;user-id=732098026;user-type= :tpanax!tpanax@tpanax.tmi.twitch.tv PRIVMSG #mistermv :@Brankhorst eve s'est inspiré du homeworld 1" }
        expected {
            Message.ChatMessage(
                id = "99d64122-2006-4e46-a43f-2b43f0ff9341",
                userId = "732098026",
                userLogin = "tpanax",
                userName = "tpanax",
                message = "@Brankhorst eve s'est inspiré du homeworld 1",
                color = "#B22222",
                isAction = false,
                embeddedEmotes = emptyList(),
                badges = emptyList(),
                isFirst = false,
                systemMsg = null,
                timestamp = Instant.parse("2022-08-23T19:01:58.667Z"),
                rewardId = null,
                inReplyTo = Message.ChatMessage.InReplyTo(
                    userName = "Brankhorst",
                    message = "On dirait EVE on line",
                    id = "7ffcf399-8d69-495c-920c-ea15a96eeee4",
                    userId = "108193474",
                    userLogin = "brankhorst",
                ),
                msgId = null,
            )
        }
    }

    @Test
    fun `Parse highlighted message`() = test {
        input { "@badge-info=subscriber/21;badges=subscriber/18,premium/1;color=#FF0000;display-name=FlorianPremier;emotes=;first-msg=0;flags=;id=68d0ad7e-7743-4b51-b8fd-3d995eb17fd5;mod=0;msg-id=highlighted-message;returning-chatter=0;room-id=135468063;subscriber=1;tmi-sent-ts=1661452379625;turbo=0;user-id=137824138;user-type= :florianpremier!florianpremier@florianpremier.tmi.twitch.tv PRIVMSG #antoinedaniel :vive l'argent" }
        expected {
            Message.ChatMessage(
                id = "68d0ad7e-7743-4b51-b8fd-3d995eb17fd5",
                userId = "137824138",
                userLogin = "florianpremier",
                userName = "FlorianPremier",
                message = "vive l'argent",
                color = "#FF0000",
                isAction = false,
                embeddedEmotes = emptyList(),
                badges = listOf(
                    Badge(
                        id = "subscriber",
                        version = "18",
                    ),
                    Badge(
                        id = "premium",
                        version = "1",
                    ),
                ),
                isFirst = false,
                systemMsg = null,
                timestamp = Instant.parse("2022-08-25T18:32:59.625Z"),
                rewardId = null,
                msgId = "highlighted-message",
                inReplyTo = null,
            )
        }
    }

    private lateinit var parser: TwitchIrcCommandParser

    @Before
    fun before() {
        parser = TwitchIrcCommandParser(Clock.System)
    }

    data class Assertion(val input: String, val expected: IrcEvent) {
        class Builder {
            private var input: String? = null
            private var expected: IrcEvent? = null

            fun input(block: () -> String) {
                input = block()
            }

            fun expected(block: () -> IrcEvent) {
                expected = block()
            }

            fun build() = Assertion(input!!, expected!!)
        }
    }

    private fun test(block: Assertion.Builder.() -> Unit) {
        val assertion = Assertion.Builder().apply(block).build()
        Assert.assertEquals(
            assertion.expected,
            parser.parse(assertion.input),
        )
    }
}
