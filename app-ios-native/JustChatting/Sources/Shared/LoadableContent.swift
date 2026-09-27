//
//  LoadableContent.swift
//  JustChatting
//

import SwiftUI

/// Shows a spinner while there is nothing to show yet, `empty` once loading is done without
/// results, and `content` otherwise.
struct LoadableContent<Content: View, Empty: View>: View {
    let isLoading: Bool
    let isEmpty: Bool
    @ViewBuilder let content: Content
    @ViewBuilder let empty: Empty

    var body: some View {
        if isEmpty && isLoading {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if isEmpty {
            empty
        } else {
            content
        }
    }
}
