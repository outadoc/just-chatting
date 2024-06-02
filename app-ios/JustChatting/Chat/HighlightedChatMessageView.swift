//
//  HighlightedChatMessageView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-22.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct HighlightedChatMessageView: View {
    var message: ChatEventMessage.Highlighted
    var body: some View {
        VStack(alignment: .leading) {
            HStack(spacing: 4) {
                if let icon = message.metadata.titleIcon?.toSystemName() {
                    Image(systemName: icon)
                }

                Text(message.metadata.title.localized())
                    .font(.system(size: 14, weight: .bold))
            }

            if let subtitle = message.metadata.subtitle {
                Text(subtitle.localized())
                    .font(.system(size: 14, weight: .bold))
            }

            Spacer()

            if let messageBody = message.body {
                ChatMessageBodyView(messageBody: messageBody)
            }
        }
    }
}

extension Icon {
    func toSystemName() -> String {
        switch self {
        case .bolt:
            "bolt"
        case .callReceived:
            "arrow.down.left"
        case .campaign:
            "megaphone"
        case .cancel:
            "x.circle"
        case .fastForward:
            "chevron.right.2"
        case .gavel:
            "person.fill.xmark"
        case .highlight:
            "highlighter"
        case .redeem:
            "gift"
        case .reply:
            "arrowshape.turn.up.left"
        case .send:
            "paperplane"
        case .star:
            "star"
        case .toll:
            "circle.dotted.and.circle"
        case .volunteerActivism:
            "giftcard"
        case .wavingHand:
            "hand.wave"
        }
    }
}
