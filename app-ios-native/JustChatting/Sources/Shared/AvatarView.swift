//
//  AvatarView.swift
//  JustChatting
//

import NukeUI
import SwiftUI

/// Round profile picture, with a placeholder while it loads or if it can't be loaded.
struct AvatarView: View {
    let url: String?
    let size: CGFloat

    var body: some View {
        LazyImage(url: url.flatMap(URL.init(string:))) { state in
            if let image = state.image {
                image.resizable().scaledToFill()
            } else {
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
    }
}
