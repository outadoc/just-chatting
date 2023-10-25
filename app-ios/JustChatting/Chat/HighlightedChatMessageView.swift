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
                .italic()

            if let subtitle = message.metadata.subtitle {
                Text(subtitle.localized())
                    .italic()
            }

            if let messageBody = message.body {
                ChatMessageBodyView(messageBody: messageBody)
            }
        }
    }
}
