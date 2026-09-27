//
//  PreviewData.swift
//  JustChatting
//

import Foundation
import JCShared

/// Mock data for SwiftUI previews, mirroring the Compose preview providers.
@MainActor
enum PreviewData {
    // MARK: - Time

    private static let instant = TimeFactoriesKt.instantFromEpochSeconds(epochSeconds:)

    private static func epochSeconds(fromNow offset: TimeInterval) -> Int64 {
        Int64(Date.now.addingTimeInterval(offset).timeIntervalSince1970)
    }

    // MARK: - Emotes and badges

    static let kappa = Emote(
        name: "Kappa",
        urls: EmoteUrls(url: "https://static-cdn.jtvnw.net/emoticons/v2/25/default/dark/3.0"),
        ownerId: nil,
        isZeroWidth: false,
        bitsValue: nil,
        colorHex: nil,
        ratio: 1
    )

    static let lul = Emote(
        name: "LUL",
        urls: EmoteUrls(url: "https://static-cdn.jtvnw.net/emoticons/v2/425618/default/dark/3.0"),
        ownerId: nil,
        isZeroWidth: false,
        bitsValue: nil,
        colorHex: nil,
        ratio: 1
    )

    static let emotes = [kappa, lul]

    static let moderatorBadge = TwitchBadge(
        setId: "moderator",
        title: "Moderator",
        version: "1",
        urls: EmoteUrls(url: "https://static-cdn.jtvnw.net/badges/v1/3267646d-33f0-4b17-b3df-f923a41db1d0/3")
    )

    static let broadcasterBadge = TwitchBadge(
        setId: "broadcaster",
        title: "Broadcaster",
        version: "1",
        urls: EmoteUrls(url: "https://static-cdn.jtvnw.net/badges/v1/5527c58c-fb7d-422d-b71b-f309dcb85cc1/3")
    )

    // MARK: - Users and streams

    private static let defaultAvatarUrl =
        "https://static-cdn.jtvnw.net/user-default-pictures-uv/cdd517fe-def4-11e9-948e-784f43822e80-profile_image-300x300.png"

    static let user = User(
        id: "1",
        login: "maghla",
        displayName: "Maghla",
        description_: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
        profileImageUrl: defaultAvatarUrl,
        createdAt: instant(epochSeconds(fromNow: -5 * 365 * 86_400)),
        usedAt: nil
    )

    static let longNameUser = User(
        id: "2",
        login: "lorem",
        displayName: "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        description_: "",
        profileImageUrl: "",
        createdAt: instant(epochSeconds(fromNow: -365 * 86_400)),
        usedAt: nil
    )

    static let category = StreamCategory(id: "1", name: "PowerWash Simulator")

    static let stream = JCShared.Stream(
        id: "1",
        userId: user.id,
        category: category,
        title: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
        viewerCount: 5_305,
        startedAt: instant(epochSeconds(fromNow: -2 * 3_600)),
        tags: [
            "French",
            "Test",
            "Sponsored",
            "Label 1",
            "Super long label with too much text, you can't really argue otherwise",
        ]
    )

    static let userStream = UserStream(user: user, stream: stream)

    static let channelFollow = ChannelFollow(
        user: user,
        followedAt: instant(epochSeconds(fromNow: -400 * 86_400))
    )

    static let searchResults: [ChannelSearchResult] = [
        ChannelSearchResult(
            title: stream.title,
            user: user,
            language: "fr",
            gameId: category.id,
            gameName: category.name,
            isLive: true,
            thumbnailUrl: nil,
            tags: ["French", "ASMR"]
        ),
        ChannelSearchResult(
            title: "",
            user: longNameUser,
            language: nil,
            gameId: nil,
            gameName: nil,
            isLive: false,
            thumbnailUrl: nil,
            tags: []
        ),
    ]

    static let scheduleSegments: [ChannelScheduleSegment] = [
        ChannelScheduleSegment(
            id: "1",
            user: user,
            startTime: instant(epochSeconds(fromNow: 86_400)),
            endTime: instant(epochSeconds(fromNow: 86_400 + 3 * 3_600)),
            title: stream.title,
            canceledUntil: nil,
            category: category
        ),
        ChannelScheduleSegment(
            id: "2",
            user: longNameUser,
            startTime: instant(epochSeconds(fromNow: 2 * 86_400)),
            endTime: nil,
            title: "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            canceledUntil: instant(epochSeconds(fromNow: 3 * 86_400)),
            category: nil
        ),
    ]

    // MARK: - Chat

    static let appUserLogin = "outadoc"

    static let chatters: [Chatter] = [
        Chatter(id: "68552712", login: "hiccoz", displayName: "Hiccoz"),
        Chatter(id: "232421548", login: "kolorye", displayName: "컬러히에"),
        Chatter(id: "280065659", login: "marion_11", displayName: "marion_11"),
    ]

    static let pronoun = Pronoun(id: "sheher", nominative: "she", objective: "her", isSingular: true)

    static func body(
        chatter: Chatter,
        message: String?,
        color: String? = nil,
        badges: [Badge] = [],
        inReplyTo: ChatListItemMessage.BodyInReplyTo? = nil,
        isAction: Bool = false
    ) -> ChatListItemMessage.Body {
        ChatListItemMessage.Body(
            messageId: UUID().uuidString,
            message: message,
            chatter: chatter,
            isAction: isAction,
            color: color,
            embeddedEmotes: [],
            gifs: [],
            badges: badges,
            inReplyTo: inReplyTo,
            sourceRoomId: nil,
            sourceBadges: [],
            isGigantifiedEmote: false
        )
    }

    static let simpleMessages: [ChatListItemMessage] = [
        ChatListItemMessage.Simple(
            body: body(
                chatter: chatters[0],
                message: "feur Kappa",
                color: "#FF69B4",
                badges: [Badge(id: "moderator", version: "1")]
            ),
            timestamp: instant(epochSeconds(fromNow: -120))
        ),
        ChatListItemMessage.Simple(
            body: body(
                chatter: chatters[1],
                message: "@djessy728 il avait dit quand mathieu avait sortit sa vidéo après longtemps "
                    + "qu'ils n'étaient plus en contact plus que ça, donc j'imagine que non",
                color: "#5F9EA0",
                inReplyTo: ChatListItemMessage.BodyInReplyTo(
                    message: "Salut Antoine, est tu encore en contact avec Mathieu? Et penses tu streamer un peu avec lui?",
                    mentions: ["djessy728"]
                )
            ),
            timestamp: instant(epochSeconds(fromNow: -90))
        ),
        ChatListItemMessage.Simple(
            body: body(
                chatter: chatters[2],
                message: "@outadoc ok att jen ai un comme vous LUL https://twitch.tv",
                color: nil,
                badges: [Badge(id: "broadcaster", version: "1")]
            ),
            timestamp: instant(epochSeconds(fromNow: -60))
        ),
        ChatListItemMessage.Simple(
            body: body(chatter: chatters[0], message: "is dancing", color: "#FF69B4", isAction: true),
            timestamp: instant(epochSeconds(fromNow: -30))
        ),
    ]

    static let highlightedMessages: [ChatListItemMessage.Highlighted] = [
        ChatListItemMessage.Highlighted(
            timestamp: instant(epochSeconds(fromNow: -300)),
            body: body(
                chatter: Chatter(id: "672551946", login: "clo_chette_", displayName: "clo_chette_"),
                message: "Top 1 on y croit",
                color: "#8A2BE2"
            ),
            metadata: ChatListItemMessage.HighlightedMetadata(
                title: StringDescRaw(value: "clo_chette_"),
                titleIcon: .star,
                subtitle: StringDescRaw(value: "subscribed at Tier 1. They've subscribed for 18 months!"),
                level: .base
            )
        ),
        ChatListItemMessage.Highlighted(
            timestamp: instant(epochSeconds(fromNow: -200)),
            body: body(
                chatter: Chatter(id: "0", login: "ravencheese", displayName: "Ravencheese"),
                message: "Lezgooooo",
                color: "#8a2be2"
            ),
            metadata: ChatListItemMessage.HighlightedMetadata(
                title: StringDescRaw(value: "First message"),
                titleIcon: .wavingHand,
                subtitle: nil,
                level: .base
            )
        ),
        ChatListItemMessage.Highlighted(
            timestamp: instant(epochSeconds(fromNow: -100)),
            body: nil,
            metadata: ChatListItemMessage.HighlightedMetadata(
                title: StringDescRaw(value: "Hype Chat"),
                titleIcon: .toll,
                subtitle: StringDescRaw(value: "€5.00"),
                level: .five
            )
        ),
    ]

    static let noticeMessages: [ChatListItemMessage.Notice] = [
        ChatListItemMessage.Notice(
            timestamp: instant(epochSeconds(fromNow: -50)),
            text: StringDescRaw(value: "This room is now in followers-only mode.")
        ),
        ChatListItemMessage.Notice(
            timestamp: instant(epochSeconds(fromNow: -40)),
            text: StringDescRaw(
                value: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin."
            )
        ),
    ]

    /// Every kind of message, like `ChatMessagePreviewProvider`.
    static let messages: [ChatListItemMessage] = simpleMessages + noticeMessages + highlightedMessages

    static let richEmbed = ChatListItemRichEmbed(
        messageId: "1",
        title: "Salut ma cocotte",
        requestUrl: "https://clips.twitch.tv",
        thumbnailUrl: "",
        authorName: "Name of a clipper",
        channelName: "Maghla"
    )

    static let chatContext = ChatMessageContext(
        emotesByName: Dictionary(uniqueKeysWithValues: emotes.map { ($0.name, $0) }),
        badges: [
            ChatMessageContext.key(setId: moderatorBadge.setId, version: moderatorBadge.version): moderatorBadge,
            ChatMessageContext.key(setId: broadcasterBadge.setId, version: broadcasterBadge.version): broadcasterBadge,
        ],
        pronouns: [chatters[1]: pronoun],
        appUserLogin: appUserLogin
    )

    static let autoCompleteItems: [AutoCompleteItem] = [
        AutoCompleteItem.User(chatter: Chatter(id: "1", login: "bagherajones", displayName: "BagheraJones")),
        AutoCompleteItem.User(chatter: Chatter(id: "2", login: "hortyunderscore", displayName: "HortyUnderscore")),
        AutoCompleteItem.Emote(emote: kappa),
        AutoCompleteItem.Emote(emote: lul),
    ]

    static let emoteSetItems: [EmoteSetItem] = [
        EmoteSetItem.Header(title: StringDescRaw(value: "Twitch"), source: nil, iconUrl: nil),
        EmoteSetItem.Emote(emote: kappa),
        EmoteSetItem.Emote(emote: lul),
        EmoteSetItem.Header(title: StringDescRaw(value: "Maghla"), source: StringDescRaw(value: "7TV"), iconUrl: nil),
    ] + (0..<12).map { _ in EmoteSetItem.Emote(emote: kappa) }

    // MARK: - Room state

    static let roomStates: [RoomState] = [
        RoomState(
            isEmoteOnly: true,
            minFollowDuration: TimeFactoriesKt.durationFromSeconds(seconds: -60),
            uniqueMessagesOnly: false,
            slowModeDuration: TimeFactoriesKt.durationFromSeconds(seconds: 0),
            isSubOnly: false
        ),
        RoomState(
            isEmoteOnly: false,
            minFollowDuration: TimeFactoriesKt.durationFromSeconds(seconds: 10 * 60),
            uniqueMessagesOnly: true,
            slowModeDuration: TimeFactoriesKt.durationFromSeconds(seconds: 30),
            isSubOnly: true
        ),
    ]

    static let messagePostConstraint = MessagePostConstraint(
        lastMessageSentAt: instant(epochSeconds(fromNow: -10)),
        slowModeDuration: TimeFactoriesKt.durationFromSeconds(seconds: 30)
    )
}
