//
//  NoticeChatMessageView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-22.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct NoticeChatMessageView: View {
    var message: ChatEventMessage.Notice
    var body: some View {
        VStack(alignment: .leading) {
            Text(message.text.localized())
                .font(.system(size: 14, weight: .bold))
        }
    }
}
