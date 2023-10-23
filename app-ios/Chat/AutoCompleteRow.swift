//
//  AutoCompleteRow.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-23.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct AutoCompleteRow: View {
    var items: [AutoCompleteItem]
    var onEmoteClick: (Emote) -> Void
    var onChatterClick: (Chatter) -> Void

    var body: some View {
        ScrollView([.horizontal]) {
            LazyHStack(spacing: 16) {
                ForEach(items, id: \.hashValue) { item in
                    switch onEnum(of: item) {
                    case let .emote(item):
                        EmoteView(
                            emote: item.emote,
                            onClick: {
                                onEmoteClick(item.emote)
                            }
                        )
                    case let .user(item):
                        Button(
                            action: {
                                onChatterClick(item.chatter)
                            },
                            label: {
                                PillView(
                                    text: item.chatter.displayName
                                )
                            }
                        )
                    }
                }
            }
            .padding([.horizontal])
        }
    }
}
