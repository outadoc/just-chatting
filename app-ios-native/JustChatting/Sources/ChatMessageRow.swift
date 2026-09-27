//
//  ChatMessageRow.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChatMessageRow: View {
    let message: ChatListItemMessage
    let context: ChatMessageContext

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            if context.showTimestamps {
                Text(message.date, format: .dateTime.hour().minute())
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .monospacedDigit()
            }

            switch onEnum(of: message) {
            case .simple(let simple):
                ChatMessageBodyView(messageBody: simple.body, context: context)
                    .padding(.vertical, 4)

            case .highlighted(let highlighted):
                HighlightedMessageView(highlighted: highlighted, context: context)

            case .notice(let notice):
                NoticeMessageView(notice: notice)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
