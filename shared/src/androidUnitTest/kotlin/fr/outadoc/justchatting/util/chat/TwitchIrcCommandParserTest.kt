package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.component.twitch.websocket.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.feature.chat.data.irc.model.IrcEvent
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEmote
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class TwitchIrcCommandParserTest {

    @Test
    fun `Parse simple PRIVMSG`() = test {
        input { "@badge-info=;badges=turbo/1;color=#0D4200;display-name=ronni;emotes=25:0-4,12-16/1902:6-10;id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;mod=0;room-id=1337;subscriber=0;tmi-sent-ts=1507246572675;turbo=1;user-id=1337;user-type=global_mod :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa Keepo Kappa" }
        expected {
            IrcEvent.Message.ChatMessage(
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
                isFirstMessageByUser = false,
                timestamp = Instant.parse("2017-10-05T23:36:12.675Z"),
                rewardId = null,
                inReplyTo = null,
                paidMessageInfo = null,
            )
        }
    }

    @Test
    fun `Parse action PRIVMSG`() = test {
        input { "@badge-info=subscriber/11;badges=moderator/1,subscriber/3009;color=#8A2BE2;display-name=pepitipepibot;emotes=;first-msg=0;flags=;id=7485e9c7-962f-4fe9-af27-3b625699f7e1;mod=1;returning-chatter=0;room-id=402890635;subscriber=1;tmi-sent-ts=1657298849487;turbo=0;user-id=651859616;user-type=mod :pepitipepibot!pepitipepibot@pepitipepibot.tmi.twitch.tv PRIVMSG #pelerine :\u0001ACTION ⭐️ RE-SUB ⭐️ Amphirae (+4) ⭐️\u0001" }
        expected {
            IrcEvent.Message.ChatMessage(
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
                isFirstMessageByUser = false,
                timestamp = Instant.parse("2022-07-08T16:47:29.487Z"),
                rewardId = null,
                inReplyTo = null,
                paidMessageInfo = null,
            )
        }
    }

    @Test
    fun `Parse mass sub gift USERNOTICE`() = test {
        input { "@badge-info=subscriber/32;badges=subscriber/2024,premium/1;color=#0000FF;display-name=ke_osiris;emotes=;flags=;id=336cdf6c-d132-41c4-9107-a232f406bebf;login=ke_osiris;mod=0;msg-id=submysterygift;msg-param-mass-gift-count=32;msg-param-origin-id=fe\\sb9\\s35\\s54\\sb5\\s01\\s71\\sd3\\sa8\\sdd\\sfb\\sb1\\s47\\s5f\\s42\\sd1\\s60\\se4\\s97\\sce;msg-param-sender-count=1688;msg-param-sub-plan=1000;room-id=135468063;subscriber=1;system-msg=ke_osiris\\sis\\sgifting\\s32\\sTier\\s1\\sSubs\\sto\\sAntoineDaniel's\\scommunity!\\sThey've\\sgifted\\sa\\stotal\\sof\\s1688\\sin\\sthe\\schannel!;tmi-sent-ts=1680550916919;user-id=563989746;user-type= :tmi.twitch.tv USERNOTICE #antoinedaniel" }
        expected {
            IrcEvent.Message.MassSubscriptionGift(
                timestamp = Instant.parse("2023-04-03T19:41:56.919Z"),
                userDisplayName = "ke_osiris",
                giftCount = 32,
                totalChannelGiftCount = 1688,
                subscriptionPlan = "1000",
            )
        }
    }

    @Test
    fun `Parse sub gift USERNOTICE`() = test {
        input { "@badge-info=subscriber/32;badges=subscriber/2024,premium/1;color=#0000FF;display-name=ke_osiris;emotes=;flags=;id=628c1219-96bc-4b26-8336-d1b8ee1a2f2a;login=ke_osiris;mod=0;msg-id=subgift;msg-param-gift-months=1;msg-param-months=6;msg-param-origin-id=fe\\sb9\\s35\\s54\\sb5\\s01\\s71\\sd3\\sa8\\sdd\\sfb\\sb1\\s47\\s5f\\s42\\sd1\\s60\\se4\\s97\\sce;msg-param-recipient-display-name=gCalim;msg-param-recipient-id=106041293;msg-param-recipient-user-name=gcalim;msg-param-sender-count=0;msg-param-sub-plan-name=Channel\\sSubscription\\s(antoinedaniellive);msg-param-sub-plan=1000;room-id=135468063;subscriber=1;system-msg=ke_osiris\\sgifted\\sa\\sTier\\s1\\ssub\\sto\\sgCalim!;tmi-sent-ts=1680550917690;user-id=563989746;user-type= :tmi.twitch.tv USERNOTICE #antoinedaniel" }
        expected {
            IrcEvent.Message.SubscriptionGift(
                timestamp = Instant.parse("2023-04-03T19:41:57.690Z"),
                userDisplayName = "ke_osiris",
                subscriptionPlan = "1000",
                recipientDisplayName = "gCalim",
                months = 1,
                cumulativeMonths = 6,
            )
        }
    }

    @Test
    fun `Parse pay forward USERNOTICE`() = test {
        input { "@badge-info=;badges=bits/100;color=#D2691E;display-name=FichierPDF;emotes=;flags=;id=5afe1c23-a512-4122-b80f-85000ac130bd;login=fichierpdf;mod=0;msg-id=communitypayforward;msg-param-prior-gifter-anonymous=false;msg-param-prior-gifter-display-name=Hipairion;msg-param-prior-gifter-id=590747111;msg-param-prior-gifter-user-name=hipairion;room-id=135468063;subscriber=1;system-msg=FichierPDF\\sis\\spaying\\sforward\\sthe\\sGift\\sthey\\sgot\\sfrom\\sHipairion\\sto\\sthe\\scommunity!;tmi-sent-ts=1682099586016;user-id=31950715;user-type= :tmi.twitch.tv USERNOTICE #antoinedaniel" }
        expected {
            IrcEvent.Message.GiftPayForward(
                timestamp = Instant.parse("2023-04-21T17:53:06.016Z"),
                userDisplayName = "FichierPDF",
                priorGifterDisplayName = "Hipairion",
            )
        }
    }

    @Test
    fun `Parse prime conversion USERNOTICE`() = test {
        input { "@badge-info=subscriber/23;badges=subscriber/18,no_video/1;color=#FF0000;display-name=PG59LeRetour;emotes=;flags=;id=510b998a-57e8-42aa-bbad-65d39107b768;login=pg59leretour;mod=0;msg-id=primepaidupgrade;msg-param-sub-plan=1000;room-id=50597026;subscriber=1;system-msg=PG59LeRetour\\sconverted\\sfrom\\sa\\sPrime\\ssub\\sto\\sa\\sTier\\s1\\ssub!;tmi-sent-ts=1681118321678;user-id=90950483;user-type= :tmi.twitch.tv USERNOTICE #ponce" }
        expected {
            IrcEvent.Message.SubscriptionConversion(
                timestamp = Instant.parse("2023-04-10T09:18:41.678Z"),
                userDisplayName = "PG59LeRetour",
                subscriptionPlan = "1000",
                userMessage = null,
            )
        }
    }

    @Test
    fun `Parse Prime sub USERNOTICE`() = test {
        input { "@badge-info=subscriber/41;badges=subscriber/36,bits/1000;color=#FFFFFF;display-name=Frfun;emotes=emotesv2_53f30305e78246aea4bc24d299dd09e7:0-5/emotesv2_f6bd60f5f3ef490aa4e40c7ee792c8c8:25-38;flags=;id=4c5a38ff-6bb3-4cad-a555-dc8a736cfc38;login=frfun;mod=0;msg-id=resub;msg-param-cumulative-months=41;msg-param-months=0;msg-param-multimonth-duration=0;msg-param-multimonth-tenure=0;msg-param-should-share-streak=1;msg-param-streak-months=40;msg-param-sub-plan-name=Channel\\sSubscription\\s(maghla);msg-param-sub-plan=Prime;msg-param-was-gifted=false;room-id=131215608;subscriber=1;system-msg=Frfun\\ssubscribed\\swith\\sPrime.\\sThey've\\ssubscribed\\sfor\\s41\\smonths,\\scurrently\\son\\sa\\s40\\smonth\\sstreak!;tmi-sent-ts=1657298959852;user-id=99037844;user-type= :tmi.twitch.tv USERNOTICE #maghla :coxPet pat pat le requin moumou4Content" }
        expected {
            IrcEvent.Message.Subscription(
                timestamp = Instant.parse("2022-07-08T16:49:19.852Z"),
                userDisplayName = "Frfun",
                months = 1,
                streakMonths = 40,
                cumulativeMonths = 41,
                subscriptionPlan = "Prime",
                userMessage = IrcEvent.Message.ChatMessage(
                    id = "4c5a38ff-6bb3-4cad-a555-dc8a736cfc38",
                    userId = "99037844",
                    userLogin = "frfun",
                    userName = "Frfun",
                    message = "coxPet pat pat le requin moumou4Content",
                    color = "#FFFFFF",
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
                    isFirstMessageByUser = false,
                    timestamp = Instant.parse("2022-07-08T16:49:19.852Z"),
                    rewardId = null,
                    inReplyTo = null,
                    paidMessageInfo = null,
                ),
            )
        }
    }

    @Test
    fun `Parse announcement USERNOTICE`() = test {
        input { "@badge-info=subscriber/11;badges=moderator/1,subscriber/3009;color=#8A2BE2;display-name=pepitipepibot;emotes=;flags=;id=54b4d931-8db5-47ad-b6e7-6687cdbbb8ec;login=pepitipepibot;mod=1;msg-id=announcement;room-id=402890635;subscriber=1;system-msg=;tmi-sent-ts=1657301015335;user-id=651859616;user-type=mod :tmi.twitch.tv USERNOTICE #pelerine :LEZGONGUE LA MIXTAPE ELLE EST LAAAAAAAA : https://open.spotify.com/album/0X9kU5VLUmXoi6Hk6ou3PP?si=85JnJJSARpqCJ_ugsGNVhQ !! Pepe a 2 track : Dig dig deep deep & Light you up !" }
        expected {
            IrcEvent.Message.Announcement(
                timestamp = Instant.parse("2022-07-08T17:23:35.335Z"),
                userMessage = IrcEvent.Message.ChatMessage(
                    id = "54b4d931-8db5-47ad-b6e7-6687cdbbb8ec",
                    userId = "651859616",
                    userLogin = "pepitipepibot",
                    userName = "pepitipepibot",
                    message = "LEZGONGUE LA MIXTAPE ELLE EST LAAAAAAAA : https://open.spotify.com/album/0X9kU5VLUmXoi6Hk6ou3PP?si=85JnJJSARpqCJ_ugsGNVhQ !! Pepe a 2 track : Dig dig deep deep & Light you up !",
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
                    rewardId = null,
                    inReplyTo = null,
                    paidMessageInfo = null,
                ),
            )
        }
    }

    @Test
    fun `Parse raid USERNOTICE`() = test {
        input { "@badge-info=;badges=hype-train/2;color=#C8C8C8;display-name=maxent__;emotes=;flags=;historical=1;id=5bca8513-e6b2-455b-a898-1cc7d3bf5332;login=maxent__;mod=0;msg-id=raid;msg-param-displayName=maxent__;msg-param-login=maxent__;msg-param-profileImageURL=https://static-cdn.jtvnw.net/jtv_user_pictures/12a5e085-db37-4e5d-b59a-77eb9f6dd8a2-profile_image-70x70.png;msg-param-viewerCount=3;rm-received-ts=1657303912918;room-id=402890635;subscriber=0;system-msg=3\\sraiders\\sfrom\\smaxent__\\shave\\sjoined!;tmi-sent-ts=1657303912832;user-id=563254735;user-type= :tmi.twitch.tv USERNOTICE #pelerine\n" }
        expected {
            IrcEvent.Message.IncomingRaid(
                timestamp = Instant.parse("2022-07-08T18:11:52.832Z"),
                raidersCount = 3,
                userDisplayName = "maxent__",
            )
        }
    }

    @Test
    fun `Parse PING`() = test {
        input { "PING :tmi.twitch.tv" }
        expected { IrcEvent.Command.Ping }
    }

    @Test
    fun `Parse simple ROOMSTATE`() = test {
        input { "@emote-only=0;followers-only=-1;r9k=0;room-id=402890635;slow=0;subs-only=0 :tmi.twitch.tv ROOMSTATE #pelerine" }
        expected {
            IrcEvent.Command.RoomStateDelta(
                isEmoteOnly = false,
                minFollowDuration = (-1).minutes,
                slowModeDuration = 0.seconds,
                uniqueMessagesOnly = false,
                isSubOnly = false,
            )
        }
    }

    @Test
    fun `Parse custom ROOMSTATE`() = test {
        input { "@emote-only=1;followers-only=15;r9k=1;room-id=402890635;slow=120;subs-only=1 :tmi.twitch.tv ROOMSTATE #pelerine" }
        expected {
            IrcEvent.Command.RoomStateDelta(
                isEmoteOnly = true,
                minFollowDuration = 15.minutes,
                slowModeDuration = 2.minutes,
                uniqueMessagesOnly = true,
                isSubOnly = true,
            )
        }
    }

    @Test
    fun `Parse delta ROOMSTATE`() = test {
        input { "@room-id=402890635;slow=0 :tmi.twitch.tv ROOMSTATE #pelerine" }
        expected {
            IrcEvent.Command.RoomStateDelta(
                slowModeDuration = 0.seconds,
            )
        }
    }

    @Test
    fun `Parse CLEARCHAT permanent ban message`() = test {
        input { "@room-id=12345678;target-user-id=87654321;tmi-sent-ts=1642715756806 :tmi.twitch.tv CLEARCHAT #dallas :ronni" }
        expected {
            IrcEvent.Command.ClearChat(
                timestamp = Instant.parse("2022-01-20T21:55:56.806Z"),
                targetUserId = "87654321",
                targetUserLogin = "ronni",
                duration = null,
            )
        }
    }

    @Test
    fun `Parse CLEARCHAT temporary ban message`() = test {
        input { "@ban-duration=350;room-id=12345678;target-user-id=87654321;tmi-sent-ts=1642719320727 :tmi.twitch.tv CLEARCHAT #dallas :ronni" }
        expected {
            IrcEvent.Command.ClearChat(
                timestamp = Instant.parse("2022-01-20T22:55:20.727Z"),
                targetUserLogin = "ronni",
                targetUserId = "87654321",
                duration = 350.seconds,
            )
        }
    }

    @Test
    fun `Parse CLEARCHAT global clear message`() = test {
        input { "@room-id=12345678;tmi-sent-ts=1642715695392 :tmi.twitch.tv CLEARCHAT #dallas" }
        expected {
            IrcEvent.Command.ClearChat(
                timestamp = Instant.parse("2022-01-20T21:54:55.392Z"),
                targetUserId = null,
                targetUserLogin = null,
                duration = null,
            )
        }
    }

    @Test
    fun `Parse USERSTATE message`() = test {
        input { "@badge-info=;badges=;color=#F85E10;display-name=outadoc;emote-sets=0,19194,553791,773027,300374282,380519965,477339272,494046698,537206155,592920959,610186276;id=e39d4a41-a7c8-4a24-845d-3e1c26aaf63f;mod=0;subscriber=0;user-type= :tmi.twitch.tv USERSTATE #mistermv" }
        expected {
            IrcEvent.Command.UserState(
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
            IrcEvent.Message.ChatMessage(
                id = "99d64122-2006-4e46-a43f-2b43f0ff9341",
                userId = "732098026",
                userLogin = "tpanax",
                userName = "tpanax",
                message = "@Brankhorst eve s'est inspiré du homeworld 1",
                color = "#B22222",
                isAction = false,
                embeddedEmotes = emptyList(),
                badges = emptyList(),
                isFirstMessageByUser = false,
                timestamp = Instant.parse("2022-08-23T19:01:58.667Z"),
                rewardId = null,
                inReplyTo = IrcEvent.Message.ChatMessage.InReplyTo(
                    userDisplayName = "Brankhorst",
                    message = "On dirait EVE on line",
                    id = "7ffcf399-8d69-495c-920c-ea15a96eeee4",
                    userId = "108193474",
                    userLogin = "brankhorst",
                ),
                paidMessageInfo = null,
            )
        }
    }

    @Test
    fun `Parse highlighted message`() = test {
        input { "@badge-info=subscriber/21;badges=subscriber/18,premium/1;color=#FF0000;display-name=FlorianPremier;emotes=;first-msg=0;flags=;id=68d0ad7e-7743-4b51-b8fd-3d995eb17fd5;mod=0;msg-id=highlighted-message;returning-chatter=0;room-id=135468063;subscriber=1;tmi-sent-ts=1661452379625;turbo=0;user-id=137824138;user-type= :florianpremier!florianpremier@florianpremier.tmi.twitch.tv PRIVMSG #antoinedaniel :vive l'argent" }
        expected {
            IrcEvent.Message.HighlightedMessage(
                timestamp = Instant.parse("2022-08-25T18:32:59.625Z"),
                userMessage = IrcEvent.Message.ChatMessage(
                    id = "68d0ad7e-7743-4b51-b8fd-3d995eb17fd5",
                    timestamp = Instant.parse("2022-08-25T18:32:59.625Z"),
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
                    isFirstMessageByUser = false,
                    rewardId = null,
                    inReplyTo = null,
                    paidMessageInfo = null,
                ),
            )
        }
    }

    @Test
    fun `Parse paid message`() = test {
        input { "@badge-info=subscriber/25;badges=subscriber/24,sub-gifter/5;color=#34BEED;display-name=Atyby;emotes=;first-msg=0;flags=;id=e63c83f4-4f4c-44fb-b62d-b1003599e61a;mod=0;pinned-chat-paid-amount=600;pinned-chat-paid-canonical-amount=600;pinned-chat-paid-currency=EUR;pinned-chat-paid-exponent=2;pinned-chat-paid-is-system-message=0;pinned-chat-paid-level=TWO;returning-chatter=0;room-id=135468063;subscriber=1;tmi-sent-ts=1687801991208;turbo=0;user-id=43868596;user-type= :atyby!atyby@atyby.tmi.twitch.tv PRIVMSG #antoinedaniel :Everybody." }
        expected {
            IrcEvent.Message.ChatMessage(
                id = "e63c83f4-4f4c-44fb-b62d-b1003599e61a",
                timestamp = Instant.parse("2023-06-26T17:53:11.208Z"),
                userId = "43868596",
                userLogin = "atyby",
                userName = "Atyby",
                message = "Everybody.",
                color = "#34BEED",
                isAction = false,
                embeddedEmotes = emptyList(),
                badges = listOf(
                    Badge(
                        id = "subscriber",
                        version = "24",
                    ),
                    Badge(
                        id = "sub-gifter",
                        version = "5",
                    ),
                ),
                isFirstMessageByUser = false,
                rewardId = null,
                inReplyTo = null,
                paidMessageInfo = IrcEvent.Message.ChatMessage.PaidMessageInfo(
                    amount = 600,
                    currency = "EUR",
                    exponent = 2,
                    isSystemMessage = false,
                    level = "TWO",
                ),
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
