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
                    Text(label(for: mode))
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

    private func formatDuration(_ seconds: Double) -> String {
        Duration.seconds(seconds).formatted(.units(allowed: [.days, .hours, .minutes, .seconds], width: .narrow))
    }
}
