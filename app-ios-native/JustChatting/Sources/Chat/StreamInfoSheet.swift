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

    @Environment(\.openURL) private var openURL

    var body: some View {
        List {
            Section {
                UserHeaderView(user: user, size: .large)

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
                        let startedAt = Date(
                            epochSeconds: stream.startedAt.epochSeconds,
                            nanosecondsOfSecond: stream.startedAt.nanosecondsOfSecond
                        )
                        Text("Live since \(startedAt.formatted(date: .omitted, time: .shortened))")
                    } icon: {
                        Image(systemName: "clock")
                    }

                    if !stream.tags.isEmpty {
                        TagList(tags: Array(stream.tags))
                            .font(.caption)
                    }
                }
            }

            Section {
                Button {
                    if let url = URL(string: "https://twitch.tv/\(user.login)") {
                        openURL(url)
                    }
                } label: {
                    Label("Watch live", systemImage: "play.tv")
                }
            }
        }
    }
}
