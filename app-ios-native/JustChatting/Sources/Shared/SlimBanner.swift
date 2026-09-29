//
//  SlimBanner.swift
//  JustChatting
//

import SwiftUI

/// Small capsule floating over content, like the chat's room modes. Glass by default, or filled
/// with `tint` for messages that need to stand out, like warnings.
struct SlimBanner<Content: View>: View {
    var tint: Color?
    @ViewBuilder let content: Content

    var body: some View {
        HStack(spacing: 12) {
            content
        }
        .font(.footnote.weight(.semibold))
        .padding(.horizontal, 12)
        .padding(.vertical, 7)
        .modifier(SlimBannerBackground(tint: tint))
    }
}

private struct SlimBannerBackground: ViewModifier {
    let tint: Color?

    func body(content: Content) -> some View {
        if let tint {
            content
                .foregroundStyle(Color.onTint)
                .background(tint, in: Capsule())
        } else {
            content
                .foregroundStyle(.secondary)
                .glassEffect(.regular, in: Capsule())
        }
    }
}
