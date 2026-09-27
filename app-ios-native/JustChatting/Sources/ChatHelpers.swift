//
//  ChatHelpers.swift
//  JustChatting
//

import JCShared
import SwiftUI

extension Date {
    /// Builds a Date from the components of a Kotlin `Instant`.
    init(epochSeconds: Int64, nanosecondsOfSecond: Int32) {
        self.init(
            timeIntervalSince1970: Double(epochSeconds)
                + Double(nanosecondsOfSecond) / 1_000_000_000
        )
    }
}

extension EmoteUrls {
    func url(for colorScheme: ColorScheme) -> URL? {
        let dict = colorScheme == .dark ? dark : light
        for scale: Float in [2.0, 1.0, 4.0] {
            if let urlStr = dict[KotlinFloat(value: scale)] as? String {
                return URL(string: urlStr)
            }
        }
        return dict.values.first.flatMap { URL(string: $0) }
    }
}

extension Color {
    /// Parses a `#RRGGBB` string.
    init?(hex: String?) {
        guard let hex, hex.hasPrefix("#"), hex.count == 7,
              let rgb = UInt64(hex.dropFirst(), radix: 16) else {
            return nil
        }
        self.init(
            red: Double((rgb >> 16) & 0xFF) / 255,
            green: Double((rgb >> 8) & 0xFF) / 255,
            blue: Double(rgb & 0xFF) / 255
        )
    }
}

extension ChatListItemMessage.Body {
    /// Whether this message can be replied to (i.e. has a Twitch message id).
    var canBeRepliedTo: Bool { messageId != nil }
}

extension ChatListItemMessage {
    /// Stable identity for list diffing, mirroring the key used by the Compose ChatList.
    var stableId: String {
        if let messageId = body?.messageId {
            return messageId
        }
        return "\(timestamp.epochSeconds).\(timestamp.nanosecondsOfSecond)-\(hash)"
    }

    var date: Date {
        Date(epochSeconds: timestamp.epochSeconds, nanosecondsOfSecond: timestamp.nanosecondsOfSecond)
    }

    /// Whether this message was deleted by a moderator, or its author timed out / banned.
    func isRedacted(by removedContent: [ChatListItemRemoveContent]) -> Bool {
        removedContent.contains { rule in
            let upUntil = Date(epochSeconds: rule.upUntil.epochSeconds, nanosecondsOfSecond: rule.upUntil.nanosecondsOfSecond)
            guard upUntil > date else { return false }
            if let messageId = rule.matchingMessageId, messageId != body?.messageId { return false }
            if let userId = rule.matchingUserId, userId != body?.chatter.id { return false }
            return true
        }
    }
}

enum ChatterColors {
    /// The chatter's own color, or a fallback from the palette if they haven't picked one.
    static func color(for chatter: Chatter, hex: String?) -> Color {
        if let color = Color(hex: hex) {
            return color
        }
        let palette = Color.chatterPalette
        // Stable per-chatter pick; String.hashValue is randomized per launch, so hash manually.
        let seed = chatter.id.unicodeScalars.reduce(UInt32(5381)) { ($0 &* 33) &+ $1.value }
        return palette[Int(seed % UInt32(palette.count))]
    }
}

extension AppPreferences {
    /// Returns a copy with the given fields replaced. Kotlin's data class `copy` requires every
    /// argument from Swift, so this is the single place that lists them all.
    func with(
        showTimestamps: Bool? = nil,
        enableRecentMessages: Bool? = nil,
        enableFfzEmotes: Bool? = nil,
        enableStvEmotes: Bool? = nil,
        enableBttvEmotes: Bool? = nil,
        enablePronouns: Bool? = nil,
        enableNotifications: Bool? = nil
    ) -> AppPreferences {
        AppPreferences(
            apiToken: apiToken,
            showTimestamps: showTimestamps ?? self.showTimestamps,
            enableRecentMessages: enableRecentMessages ?? self.enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes ?? self.enableFfzEmotes,
            enableStvEmotes: enableStvEmotes ?? self.enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes ?? self.enableBttvEmotes,
            enablePronouns: enablePronouns ?? self.enablePronouns,
            enableNotifications: enableNotifications ?? self.enableNotifications
        )
    }
}
