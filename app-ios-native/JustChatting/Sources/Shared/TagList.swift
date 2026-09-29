//
//  TagList.swift
//  JustChatting
//

import SwiftUI

/// A single stream tag. Uses the environment's font.
struct TagChip: View {
    let tag: String

    var body: some View {
        Text(tag)
            .lineLimit(1)
            .foregroundStyle(.secondary)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(.fill.tertiary, in: RoundedRectangle(cornerRadius: 8))
    }
}

/// Stream tags, wrapped over as many lines as needed. Uses the environment's font.
struct TagList: View {
    let tags: [String]

    var body: some View {
        FlowLayout(spacing: 6) {
            ForEach(tags, id: \.self) { tag in
                TagChip(tag: tag)
            }
        }
    }
}

/// Stream tags on a single line: as many as fit, then how many were left out. Uses the
/// environment's font.
struct TagLine: View {
    let tags: [String]

    var body: some View {
        if !tags.isEmpty {
            // Try showing every tag, then fewer until they fit.
            ViewThatFits(in: .horizontal) {
                ForEach((1...tags.count).reversed(), id: \.self) { count in
                    line(showing: count)
                }
            }
        }
    }

    private func line(showing count: Int) -> some View {
        HStack(spacing: 6) {
            ForEach(tags.prefix(count), id: \.self) { tag in
                TagChip(tag: tag)
            }
            if tags.count > count {
                Text(verbatim: "+\(tags.count - count)")
                    .foregroundStyle(.secondary)
                    .accessibilityLabel(Text("\(tags.count - count) more tags"))
            }
        }
    }
}
