//
//  HighlightedMessageView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct HighlightedMessageView: View {
    let highlighted: ChatListItemMessage.Highlighted
    let context: ChatMessageContext

    var body: some View {
        let accentColor = color(forLevel: highlighted.metadata.level)
        HStack(spacing: 0) {
            accentColor.frame(width: 4)
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 4) {
                    if let icon = highlighted.metadata.titleIcon {
                        Image(systemName: symbolName(forIcon: icon))
                            .font(.callout)
                    }
                    Text(highlighted.metadata.title.localizedString())
                        .font(.callout)
                        .fontWeight(.semibold)
                }
                .foregroundStyle(accentColor)

                if let subtitle = highlighted.metadata.subtitle {
                    Text(subtitle.localizedString())
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                if let body = highlighted.body {
                    ChatMessageBodyView(messageBody: body, context: context)
                }
            }
            .padding(8)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemBackground))
        }
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .padding(.vertical, 4)
    }

    private func color(forLevel level: ChatListItemMessage.HighlightedLevel) -> Color {
        switch level {
        case .base:  return .accentColor
        case .one:   return .highlightLevel(1)
        case .two:   return .highlightLevel(2)
        case .three: return .highlightLevel(3)
        case .four:  return .highlightLevel(4)
        case .five:  return .highlightLevel(5)
        case .six:   return .highlightLevel(6)
        case .seven: return .highlightLevel(7)
        case .eight: return .highlightLevel(8)
        case .nine:  return .highlightLevel(9)
        case .ten:   return .highlightLevel(10)
        }
    }

    private func symbolName(forIcon icon: Icon) -> String {
        switch icon {
        case .callReceived:      return "phone.arrow.down.left"
        case .campaign:          return "megaphone.fill"
        case .cancel:            return "xmark.circle.fill"
        case .fastForward:       return "forward.fill"
        case .gavel:             return "hammer.fill"
        case .highlight:         return "star.fill"
        case .redeem:            return "gift.fill"
        case .reply:             return "arrowshape.turn.up.left.fill"
        case .send:              return "paperplane.fill"
        case .star:              return "star.fill"
        case .toll:              return "bell.fill"
        case .volunteerActivism: return "heart.fill"
        case .wavingHand:        return "hand.wave.fill"
        default:                 return "info.circle.fill"
        }
    }
}
