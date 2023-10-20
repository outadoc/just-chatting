//
//  ChattingView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-20.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import SwiftUI

struct ChattingView: View {
    var channelLogin: String
    var body: some View {
        Text("\(channelLogin)")
            .navigationTitle(channelLogin)
    }
}

struct ChattingView_Previews: PreviewProvider {
    static var previews: some View {
        ChattingView(channelLogin: "HortyUnderscore")
    }
}
