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
                HStack(spacing: 12) {
                    AsyncImage(url: URL(string: user.profileImageUrl)) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color.secondary.opacity(0.3)
                    }
                    .frame(width: 56, height: 56)
                    .clipShape(Circle())

                    VStack(alignment: .leading, spacing: 2) {
                        Text(user.displayName)
                            .font(.title3.weight(.semibold))
                        Text(verbatim: "@\(user.login)")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

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

                    let tags = Array(stream.tags)
                    if !tags.isEmpty {
                        FlowLayout(spacing: 4) {
                            ForEach(tags, id: \.self) { tag in
                                Text(tag)
                                    .font(.caption)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(.quaternary, in: Capsule())
                            }
                        }
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

#Preview("Live") {
    StreamInfoSheet(user: PreviewData.user, stream: PreviewData.stream)
}

#Preview("Offline") {
    StreamInfoSheet(user: PreviewData.longNameUser, stream: nil)
}
