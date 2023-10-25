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
            Text(message.metadata.title.localized())
                .font(.system(size: 14, weight: .bold))

            if let subtitle = message.metadata.subtitle {
                Text(subtitle.localized())
                    .font(.system(size: 14, weight: .bold))
            }

            if let messageBody = message.body {
                ChatMessageBodyView(messageBody: messageBody)
            }
        }
    }
}
