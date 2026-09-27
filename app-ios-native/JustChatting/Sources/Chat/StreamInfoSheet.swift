//
//  StreamInfoSheet.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Details about the channel whose chat is open, and its stream if it is live.
struct StreamInfoSheet: View {
    let user: User
    let stream: JCShared.Stream?

    var body: some View {
        List {
            Section {
                UserHeaderView(user: user)

                if !user.description_.isEmpty {
                    Text(user.description_)
                        .font(.callout)
                }
            }

            if let stream {
                Section("Stream") {
                    Text(stream.title)
                        .font(.body.weight(.medium))

                    if let category = stream.category {
                        Label(category.name, systemImage: "gamecontroller")
                    }

                    Label {
                        Text("\(Int(stream.viewerCount).formatted()) viewers")
                    } icon: {
                        Image(systemName: "person.2.fill")
                    }

                    Label {
                        Text("Live since \(stream.startedAt.date.formatted(date: .omitted, time: .shortened))")
                    } icon: {
                        Image(systemName: "clock")
                    }

                    if !stream.tags.isEmpty {
                        TagList(tags: stream.tags)
                            .font(.caption)
                    }
                }
            }

            if let url = user.channelUrl {
                Section {
                    Link(destination: url) {
                        Label("Watch live", systemImage: "play.tv")
                    }
                }
            }
        }
    }
}
