//
//  DetailsListRow.swift
//  JustChatting
//

import SwiftUI

/// List row for the lists shown in details sheets, such as the emotes used in a message or the
/// channels in a shared chat: an image on the leading edge, a title, and an optional trailing value.
struct DetailsListRow<Icon: View, Trailing: View>: View {
    let title: String
    /// Builds the leading image, given the height it should be drawn at.
    @ViewBuilder let icon: (_ height: CGFloat) -> Icon
    @ViewBuilder let trailing: Trailing

    @ScaledMetric(relativeTo: .body) private var iconHeight: CGFloat = 32

    var body: some View {
        LabeledContent {
            trailing
        } label: {
            Label {
                Text(title)
                    .lineLimit(1)
                    .foregroundStyle(Color.primary)
            } icon: {
                icon(iconHeight)
            }
        }
    }
}

extension DetailsListRow where Trailing == EmptyView {
    init(title: String, @ViewBuilder icon: @escaping (_ height: CGFloat) -> Icon) {
        self.init(title: title, icon: icon) { EmptyView() }
    }
}
