//
//  NoticeMessageView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct NoticeMessageView: View {
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

#Preview {
    VStack(spacing: 8) {
        ForEach(PreviewData.noticeMessages, id: \.self) { notice in
            NoticeMessageView(notice: notice)
        }
    }
    .padding()
}
