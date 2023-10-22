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
    var inputMessage: String
    var updateInputMessage: (String) -> Void
    var submit: () -> Void

    var messages: [ChatEventMessage]

    @State private var currentPosition: Int?
    @State private var isAtBottom: Bool = false
    @State private var isTextFieldFocused: Bool = false

    private var inputMessageBinding: Binding<String> {
        Binding(
            get: { self.inputMessage },
            set: { value in updateInputMessage(value) }
        )
    }

    var body: some View {
        VStack {
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
                    .scrollDismissesKeyboard(.interactively)
                }
            }
            .frame(maxHeight: .infinity)

            HStack(spacing: 8) {
                TextField(
                    MR.strings.shared.chat_input_hint.desc().localized(),
                    text: inputMessageBinding,
                    prompt: Text(MR.strings.shared.chat_input_hint.desc().localized())
                )
                .textFieldStyle(.roundedBorder)
                .focusable(isTextFieldFocused)
                .onSubmit(submit)

                if !inputMessage.isEmpty {
                    Button(
                        action: submit,
                        label: {
                            Image(systemName: "paperplane")
                        }
                    )
                }
            }
            .padding(16)
        }
        .onChange(of: isTextFieldFocused) {
            if let lastMessage = messages.last {
                if isTextFieldFocused {
                    currentPosition = lastMessage.hashValue
                }
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
