//
//  ChatEventsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Banners pinned to the top of the chat: room modes and connection status.
struct ChatEventsView: View {
    let chatting: ChatViewModel.StateChatting

    var body: some View {
        VStack(spacing: 6) {
            RoomStateBanner(roomState: chatting.roomState)

            if !chatting.connectionStatus.isAlive {
                SlimBanner(tint: .warning) {
                    Label("Reconnecting to chat…", systemImage: "wifi.exclamationmark")
                }
            }
        }
        .padding(.horizontal, 8)
        .padding(.top, 4)
        .animation(.default, value: chatting.roomState)
        .animation(.default, value: chatting.connectionStatus.isAlive)
    }
}

private struct SlimBanner<Content: View>: View {
    var tint: Color = .accentColor
    @ViewBuilder let content: Content

    var body: some View {
        HStack(spacing: 12) {
            content
        }
        .font(.footnote.weight(.medium))
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(tint, in: Capsule())
        .foregroundStyle(Color.onTint)
    }
}

private struct RoomStateBanner: View {
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

/// Thin bar showing how long until the user can send another message in slow mode.
struct SlowModeProgressView: View {
    let constraint: MessagePostConstraint

    var body: some View {
        let lastSent = Date(
            epochSeconds: constraint.lastMessageSentAt.epochSeconds,
            nanosecondsOfSecond: constraint.lastMessageSentAt.nanosecondsOfSecond
        )
        let duration = constraint.slowModeDurationSeconds
        let end = lastSent.addingTimeInterval(duration)

        TimelineView(.animation(minimumInterval: 0.1, paused: Date() >= end)) { timeline in
            let remaining = end.timeIntervalSince(timeline.date)
            if remaining > 0, duration > 0 {
                ProgressView(value: remaining / duration)
                    .progressViewStyle(.linear)
                    .accessibilityLabel("Slow mode")
                    .accessibilityValue("\(Int(remaining.rounded(.up))) seconds remaining")
            }
        }
    }
}
