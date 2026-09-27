//
//  ExternalLink.swift
//  JustChatting
//

import SwiftUI

/// List row that opens a URL outside of the app, with an icon hinting at it.
struct ExternalLink<Content: View>: View {
    let destination: URL
    @ViewBuilder let label: Content

    var body: some View {
        Link(destination: destination) {
            HStack {
                label
                    .foregroundStyle(.primary)
                Spacer()
                Image(systemName: "arrow.up.right.square")
                    .foregroundStyle(.secondary)
            }
        }
    }
}

extension ExternalLink where Content == Text {
    init(_ title: LocalizedStringKey, destination: URL) {
        self.init(destination: destination) {
            Text(title)
        }
    }
}
