//
//  EmotePicker.swift
//  JustChatting
//

import JCShared
import NukeUI
import SwiftUI

/// Grid of every emote usable in the current chat, grouped by emote set (recent emotes first).
struct EmotePicker: View {
    let items: [EmoteSetItem]
    let onEmoteClick: (Emote) -> Void

    @ScaledMetric(relativeTo: .body) private var emoteHeight: CGFloat = 32

    private struct EmoteSection: Identifiable {
        let id: Int
        let header: EmoteSetItem.Header?
        var emotes: [PickerEmote]
    }

    /// An emote at a given place in the picker. Its identity must be unique across the whole
    /// grid, not just within its section: lazy grids otherwise mix up cells from different
    /// sections that share the same identity, and show the wrong emote after scrolling.
    private struct PickerEmote: Identifiable {
        let id: String
        let emote: Emote
    }

    var body: some View {
        let sections = makeSections()
        ScrollView {
            LazyVGrid(
                columns: [GridItem(.adaptive(minimum: emoteHeight + 16), spacing: 4)],
                spacing: 4,
                pinnedViews: [.sectionHeaders]
            ) {
                ForEach(sections) { section in
                    Section {
                        ForEach(section.emotes) { item in
                            Button {
                                onEmoteClick(item.emote)
                            } label: {
                                EmoteView(emote: item.emote, height: emoteHeight)
                                    .frame(maxWidth: .infinity, minHeight: emoteHeight + 12)
                                    .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                        }
                    } header: {
                        if let header = section.header {
                            sectionHeader(header)
                        }
                    }
                }
            }
            .padding(.horizontal, 8)
        }
    }

    private func sectionHeader(_ header: EmoteSetItem.Header) -> some View {
        HStack(spacing: 8) {
            if let iconUrl = header.iconUrl, let url = URL(string: iconUrl) {
                LazyImage(url: url) { state in
                    if let image = state.image {
                        image.resizable().scaledToFill()
                    } else {
                        Color.secondary.opacity(0.3)
                    }
                }
                .frame(width: 20, height: 20)
                .clipShape(Circle())
            }
            if let title = header.title {
                Text(title.localizedString())
                    .font(.subheadline.weight(.semibold))
            }
            if let source = header.source {
                Text(source.localizedString())
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding(.vertical, 6)
        .background(.bar)
        .accessibilityAddTraits(.isHeader)
    }

    private func makeSections() -> [EmoteSection] {
        var sections: [EmoteSection] = []
        for item in items {
            switch onEnum(of: item) {
            case .header(let header):
                sections.append(EmoteSection(id: sections.count, header: header, emotes: []))
            case .emote(let emote):
                if sections.isEmpty {
                    sections.append(EmoteSection(id: 0, header: nil, emotes: []))
                }
                let section = sections.count - 1
                // Includes the position, since the same emote can appear twice in a set.
                let id = "\(section)/\(sections[section].emotes.count)/\(emote.emote.name)"
                sections[section].emotes.append(PickerEmote(id: id, emote: emote.emote))
            }
        }
        return sections.filter { !$0.emotes.isEmpty }
    }
}
