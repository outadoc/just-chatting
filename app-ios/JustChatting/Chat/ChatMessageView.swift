//
//  ChatMessageView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-22.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct ChatMessageView: View {
    var message: ChatEventMessage
    var body: some View {
        ZStack {
            switch onEnum(of: message) {
            case let .simple(message):
                SimpleChatMessageView(message: message)
            case let .notice(message):
                NoticeChatMessageView(message: message)
            case let .highlighted(message):
                HighlightedChatMessageView(message: message)
            }
        }
    }
}
