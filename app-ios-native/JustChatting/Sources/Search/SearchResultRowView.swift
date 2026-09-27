//
//  SearchResultRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SearchResultRowView: View {
    let result: ChannelSearchResult

    var body: some View {
        ChannelRow(user: result.user, isLive: result.isLive) {
            if !result.title.isEmpty {
                ChannelRowTitle(text: result.title)
            }

            if result.gameName != nil {
                ChannelRowCategoryLine(category: result.gameName)
            }
        }
        // The avatar's ring is the only visual cue.
        .accessibilityValue(result.isLive ? Text("Live") : Text(verbatim: ""))
    }
}
