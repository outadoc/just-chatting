//
//  RoomStateBanner.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct RoomStateBanner: View {
    let roomState: RoomState

    var body: some View {
        let modes = roomState.activeModes
        if !modes.isEmpty {
            SlimBanner {
                ForEach(Array(modes.enumerated()), id: \.offset) { _, mode in
                    Label(label(for: mode), systemImage: symbolName(for: mode))
                }
            }
            .accessibilityElement(children: .combine)
        }
    }

    private func label(for mode: RoomMode) -> String {
        switch onEnum(of: mode) {
        case .emoteOnly:
            String(localized: "Emote")
        case .followersOnly(let followersOnly):
            followersOnly.minFollowDurationSeconds == 0
                ? String(localized: "Followers")
                : String(localized: "Followers \(formatDuration(followersOnly.minFollowDurationSeconds))")
        case .uniqueMessages:
            String(localized: "Unique")
        case .slow(let slow):
            String(localized: "Slow \(formatDuration(slow.delaySeconds))")
        case .subscribersOnly:
            String(localized: "Subs")
        }
    }

    private func symbolName(for mode: RoomMode) -> String {
        switch onEnum(of: mode) {
        case .emoteOnly: "face.smiling"
        case .followersOnly: "heart.fill"
        case .uniqueMessages: "text.badge.checkmark"
        case .slow: "hourglass"
        case .subscribersOnly: "star.fill"
        }
    }

    private func formatDuration(_ seconds: Double) -> String {
        Duration.seconds(seconds).formatted(.units(allowed: [.days, .hours, .minutes, .seconds], width: .narrow))
    }
}
