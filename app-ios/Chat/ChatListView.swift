//
//  ChatListView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-22.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct ChatListView: View {
    var messages: [ChatEventMessage]
    var body: some View {
        List {
            ForEach(messages, id: \.timestamp) { message in
                ChatMessageView(message: message)
            }
        }
        .listStyle(.inset)
    }
}

struct ChatMessageView: View {
    var message: ChatEventMessage
    var body: some View {
        ZStack {
            switch onEnum(of: message) {
            case let .simple(message):
                SimpleChatMessage(message: message)
            case let .notice(message):
                NoticeChatMessage(message: message)
            case let .highlighted(message):
                HighlightedChatMessage(message: message)
            }
        }
    }
}

struct SimpleChatMessage: View {
    var message: ChatEventMessage.Simple
    var body: some View {
        VStack {
            ChatMessageBody(messageBody: message.body)
        }
    }
}

struct NoticeChatMessage: View {
    var message: ChatEventMessage.Notice
    var body: some View {
        VStack {
            if let messageBody = message.body {
                ChatMessageBody(messageBody: messageBody)
            }
        }
    }
}

struct HighlightedChatMessage: View {
    var message: ChatEventMessage.Highlighted
    var body: some View {
        VStack {
            if let messageBody = message.body {
                ChatMessageBody(messageBody: messageBody)
            }
        }
    }
}

struct ChatMessageBody: View {
    var messageBody: ChatEventMessage.Body
    var body: some View {
        VStack {
            if let message = messageBody.message {
                Text(message)
            }
        }
    }
}
