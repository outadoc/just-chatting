//
//  LiveChannelItemView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import Flow
import JCShared
import SwiftUI

struct LiveChannelItemView: View {
    var stream: JCShared.Stream
    var body: some View {
        HStack(spacing: 16) {
            if let avatarUrl = stream.profileImageURL {
                AvatarView(url: URL(string: avatarUrl)!)
            }

            VStack(alignment: .leading) {
                Text(stream.title)
                    .lineLimit(1)
                    .bold()

                Text(stream.userName)
                    .lineLimit(1)

                if let gameName = stream.gameName {
                    Text(gameName)
                        .lineLimit(1)
                }

                HFlow {
                    ForEach(stream.tags, id: \.self) { tag in
                        PillView(text: tag)
                            .font(.footnote)
                    }
                }
            }
        }
    }
}

struct LiveChannelItemView_Previews: PreviewProvider {
    static var previews: some View {
        LiveChannelItemView(
            stream: Stream(
                id: "",
                userId: "",
                userLogin: "antoinedaniel",
                userName: "AntoineDaniel",
                gameName: "Professor Layton",
                title: "LAYTON MAINTENANT VENEZ TOUT DE SUITE",
                viewerCount: 10000,
                startedAt: "",
                profileImageURL: "",
                tags: ["feur", "coubeh"]
            )
        )
        .previewLayout(PreviewLayout.sizeThatFits)
        .padding()
    }
}
