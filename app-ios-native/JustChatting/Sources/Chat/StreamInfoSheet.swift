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

    /// Absent outside of the logged-in UI, e.g. in previews.
    @Environment(AppRouter.self) private var router: AppRouter?
    @Environment(\.dismiss) private var dismiss

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
                        // Lists tint label icons with the accent color, which is also this
                        // button's background: draw the whole label in the on-tint color instead.
                        Label("Watch live", systemImage: "play.fill")
                            .foregroundStyle(Color.onTint)
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
                        Button {
                            dismiss()
                            router?.openChannel(userId: channel.user.id)
                        } label: {
                            DetailsListRow(title: channel.user.displayName) { height in
                                AvatarView(url: channel.user.profileImageUrl, size: height)
                            } trailing: {
                                if channel.isHost {
                                    Text("Host")
                                }
                            }
                        }
                        .accessibilityHint("Opens this channel's chat")
                    }
                }
            }
        }
        .listSectionSpacing(.compact)
    }
}
