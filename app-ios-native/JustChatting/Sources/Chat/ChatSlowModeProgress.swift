//
//  ChatSlowModeProgress.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Thin bar showing how long until the user can send another message in slow mode.
struct ChatSlowModeProgress: View {
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
