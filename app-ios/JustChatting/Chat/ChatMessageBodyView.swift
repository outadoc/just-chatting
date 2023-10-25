//
//  ChatMessageBodyView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-22.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct ChatMessageBodyView: View {
    var messageBody: ChatEventMessage.Body

    private var userNameString: AttributedString {
        var attributedString = AttributedString("\(messageBody.chatter.displayName): ")
        attributedString.font = .boldSystemFont(ofSize: 14)
        if let color = messageBody.color {
            attributedString.foregroundColor = color.parseHex()
        }
        return attributedString
    }

    private var messageString: AttributedString {
        var attributedString = AttributedString(messageBody.message ?? "")
        attributedString.font = .systemFont(ofSize: 14)
        return attributedString
    }

    var body: some View {
        HStack {
            Text(userNameString + messageString)
        }
    }
}
