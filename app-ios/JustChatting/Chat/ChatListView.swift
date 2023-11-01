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
    var autoCompleteItems: [AutoCompleteItem]
    var inputMessage: String
    var updateInputMessage: (String) -> Void
    var submit: () -> Void
    var onEmoteClick: (Emote) -> Void
    var onChatterClick: (Chatter) -> Void

    private let inputHeight = 96.0

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
        ZStack(alignment: .bottomTrailing) {
            ZStack {
                if messages.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    ScrollView(.vertical) {
                        LazyVStack(alignment: .leading) {
                            ForEach(messages.indices, id: \.self) { index in
                                let message = messages[index]

                                VStack(alignment: .leading) {
                                    Spacer().frame(maxWidth: .infinity)

                                    ChatMessageView(message: message)
                                        .onAppear {
                                            if index == messages.count - 1 {
                                                isAtBottom = true
                                            }
                                        }
                                        .onDisappear {
                                            if index == messages.count - 1 {
                                                isAtBottom = false
                                            }
                                        }
                                        .padding([.horizontal])

                                    Spacer().frame(maxWidth: .infinity)
                                }
                                .frame(maxWidth: .infinity)
                                .background(
                                    Color(
                                        index % 2 == 0 ? .systemBackground : .secondarySystemBackground
                                    )
                                )
                            }
                        }
                        .scrollTargetLayout()
                        .padding([.bottom], inputHeight)
                    }
                    .scrollTargetBehavior(.viewAligned)
                    .scrollPosition(id: $currentPosition)
                    .scrollDismissesKeyboard(.interactively)
                }
            }
            .frame(maxHeight: .infinity)

            if !isAtBottom {
                Button(
                    action: {
                        if messages.count > 1 {
                            currentPosition = messages.count - 1
                        }
                    },
                    label: {
                        Image(systemName: "chevron.down")
                            .padding()
                            .background(.bar)
                            .clipShape(Circle())
                    }
                )
                .padding(24)
                .padding([.bottom], inputHeight)
            }

            VStack {
                AutoCompleteRow(
                    items: autoCompleteItems,
                    onEmoteClick: onEmoteClick,
                    onChatterClick: onChatterClick
                )

                HStack(spacing: 16) {
                    TextField(
                        MR.strings.shared.chat_input_hint.desc().localized(),
                        text: inputMessageBinding,
                        prompt: Text(MR.strings.shared.chat_input_hint.desc().localized())
                    )
                    .keyboardType(.alphabet)
                    .submitLabel(.send)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
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
                .padding([.horizontal, .bottom])
            }
            .frame(height: inputHeight)
            .background(.bar)
        }
        .onChange(of: isTextFieldFocused) {
            if messages.count > 1 {
                if isTextFieldFocused {
                    currentPosition = messages.count - 1
                }
            }
        }
        .onChange(of: messages.count) {
            if messages.count > 1 {
                if isAtBottom || currentPosition == nil {
                    currentPosition = messages.count - 1
                }
            }
        }
    }
}
