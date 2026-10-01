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
    var sharedChatChannels: [SharedChatChannel] = []

    var body: some View {
        List {
            Section {
                UserHeaderView(user: user, showsCreationDate: true)

                if !user.description_.isEmpty {
                    Text(user.description_)
                }
            }

            if let url = user.channelUrl {
                Section {
                    Link(destination: url) {
                        Label("Watch live", systemImage: "play.fill")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.glassProminent)
                    .controlSize(.large)
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
                }
            }

            if let stream {
                Section("Live") {
                    Text(stream.title)
                        .font(.headline)

                    if let category = stream.category {
                        Label(category.name, systemImage: "gamecontroller")
                    }

                    Label {
                        Text("\(Int(stream.viewerCount).formatted()) viewers")
                    } icon: {
                        Image(systemName: "person.2")
                    }

                    Label {
                        Text("Live since \(stream.startedAt.date.formatted(date: .omitted, time: .shortened))")
                    } icon: {
                        Image(systemName: "clock")
                    }

                    if !stream.tags.isEmpty {
                        TagList(tags: stream.tags)
                            .font(.footnote)
                    }
                }
            }

            if !sharedChatChannels.isEmpty {
                Section("Shared chat") {
                    ForEach(sharedChatChannels, id: \.user.id) { channel in
                        HStack(spacing: 12) {
                            AvatarView(url: channel.user.profileImageUrl, size: 24)
                            Text(channel.user.displayName)
                            Spacer(minLength: 0)
                            if channel.isHost {
                                Text("Host")
                                    .font(.footnote.weight(.semibold))
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
        }
        .listSectionSpacing(.compact)
    }
}
