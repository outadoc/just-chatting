//
//  NoticeMessage.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct NoticeMessage: View {
    let notice: ChatListItemMessage.Notice

    var body: some View {
        HStack(spacing: 0) {
            Color.accentColor.frame(width: 4)
            Text(notice.text.localizedString())
                .font(.callout)
                .padding(8)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.secondarySystemBackground))
        }
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .padding(.vertical, 4)
    }
}
