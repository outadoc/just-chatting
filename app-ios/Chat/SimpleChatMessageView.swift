//
//  SimpleChatMessageView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-22.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct SimpleChatMessageView: View {
    var message: ChatEventMessage.Simple
    var body: some View {
        VStack {
            ChatMessageBodyView(messageBody: message.body)
        }
    }
}
