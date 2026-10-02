//
//  RaidGoCard.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Shown when the channel raids another one; tapping it opens the raided channel's chat.
struct RaidGoCard: View {
    let raid: Raid.Go

    /// Absent outside of the logged-in UI, e.g. in previews.
    @Environment(AppRouter.self) private var router: AppRouter?

    var body: some View {
        Button {
            router?.openChannel(userId: raid.targetId)
        } label: {
            HStack(spacing: 12) {
                AvatarView(url: raid.targetProfileImageUrl, size: 44)

                VStack(alignment: .leading, spacing: 2) {
                    Text("Raid has begun")
                        .font(.footnote.weight(.semibold))
                        .foregroundStyle(.secondary)
                    Text("Everybody join @\(raid.targetDisplayName)!")
                        .font(.headline)
                        .lineLimit(2)
                }

                Spacer(minLength: 0)

                Image(systemName: "arrow.right")
                    .font(.body.weight(.semibold))
                    .foregroundStyle(Color.accentColor)
            }
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .glassEffect(.regular.interactive(), in: RoundedRectangle(cornerRadius: 18))
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityHint(Text("Join the raid"))
        .accessibilityAddTraits(.isLink)
    }
}
