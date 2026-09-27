//
//  TagList.swift
//  JustChatting
//

import SwiftUI

/// Stream tags, wrapped over as many lines as needed. Uses the environment's font.
struct TagList: View {
    let tags: [String]

    var body: some View {
        FlowLayout(spacing: 4) {
            ForEach(tags, id: \.self) { tag in
                Text(tag)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(.quaternary, in: Capsule())
            }
        }
    }
}
