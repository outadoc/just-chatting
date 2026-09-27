//
//  SlimBanner.swift
//  JustChatting
//

import SwiftUI

struct SlimBanner<Content: View>: View {
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

#Preview {
    VStack(spacing: 8) {
        SlimBanner {
            Text("Emote")
            Text("Followers")
            Text("Unique")
        }
        SlimBanner(tint: .warning) {
            Label("Reconnecting to chat…", systemImage: "wifi.exclamationmark")
        }
    }
    .padding()
}
