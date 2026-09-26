//
//  EmotePickerView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Grid of every emote usable in the current chat, grouped by emote set (recent emotes first).
struct EmotePickerView: View {
    let items: [EmoteSetItem]
    let onEmoteClick: (Emote) -> Void

    @ScaledMetric(relativeTo: .body) private var emoteHeight: CGFloat = 32

    private struct EmoteSection: Identifiable {
        let id: Int
        let header: EmoteSetItem.Header?
        var emotes: [Emote]
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
                        ForEach(Array(section.emotes.enumerated()), id: \.offset) { _, emote in
                            Button {
                                onEmoteClick(emote)
                            } label: {
                                EmoteView(emote: emote, height: emoteHeight)
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
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color.secondary.opacity(0.3)
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
                sections[sections.count - 1].emotes.append(emote.emote)
            }
        }
        return sections.filter { !$0.emotes.isEmpty }
    }
}
