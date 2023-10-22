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

    @State var currentPosition: Int?
    @State var isAtBottom: Bool = false

    var body: some View {
        ZStack {
            if messages.isEmpty {
                ProgressView()
            } else {
                ScrollView(.vertical) {
                    LazyVStack(alignment: .leading) {
                        ForEach(messages, id: \.hashValue) { message in
                            Divider()
                            ChatMessageView(message: message)
                                .onAppear {
                                    if message == messages.last {
                                        isAtBottom = true
                                    }
                                }
                                .onDisappear {
                                    if message == messages.last {
                                        isAtBottom = false
                                    }
                                }
                        }
                    }
                    .scrollTargetLayout()
                }
                .safeAreaPadding(.horizontal, 16.0)
                .scrollTargetBehavior(.viewAligned)
                .scrollPosition(id: $currentPosition)
            }
        }
        .onChange(of: messages.count) {
            if let lastMessage = messages.last {
                if isAtBottom || currentPosition == nil {
                    currentPosition = lastMessage.hashValue
                }
            }
        }
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
                var userName = AttributedString("\(messageBody.chatter.displayName): ")
                // userName.font = .largeTitle
                Text(userName + AttributedString(message))
            }
        }
    }
}
