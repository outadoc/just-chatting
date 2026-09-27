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
