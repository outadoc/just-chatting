//
//  RoomStateBanner.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct RoomStateBanner: View {
    let roomState: RoomState

    var body: some View {
        let modes = activeModes
        if !modes.isEmpty {
            SlimBanner {
                ForEach(modes, id: \.self) { mode in
                    Text(mode)
                }
            }
            .accessibilityElement(children: .combine)
        }
    }

    private var activeModes: [String] {
        var modes: [String] = []
        if roomState.isEmoteOnly {
            modes.append(String(localized: "Emote"))
        }
        let minFollow = roomState.minFollowDurationSeconds
        if minFollow == 0 {
            modes.append(String(localized: "Followers"))
        } else if minFollow > 0 {
            modes.append(String(localized: "Followers \(formatDuration(minFollow))"))
        }
        if roomState.uniqueMessagesOnly {
            modes.append(String(localized: "Unique"))
        }
        if roomState.slowModeDurationSeconds > 0 {
            modes.append(String(localized: "Slow \(formatDuration(roomState.slowModeDurationSeconds))"))
        }
        if roomState.isSubOnly {
            modes.append(String(localized: "Subs"))
        }
        return modes
    }

    private func formatDuration(_ seconds: Double) -> String {
        Duration.seconds(seconds).formatted(.units(allowed: [.days, .hours, .minutes, .seconds], width: .narrow))
    }
}

#Preview {
    VStack(spacing: 8) {
        ForEach(PreviewData.roomStates, id: \.self) { roomState in
            RoomStateBanner(roomState: roomState)
        }
    }
    .padding()
}
