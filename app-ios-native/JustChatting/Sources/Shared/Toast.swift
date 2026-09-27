//
//  Toast.swift
//  JustChatting
//

import SwiftUI

extension View {
    /// Briefly shows `message` at the top of this view every time `trigger` changes. Changing it
    /// again while the message is visible keeps it on screen for longer.
    func toast<Trigger: Equatable>(_ message: LocalizedStringKey, trigger: Trigger) -> some View {
        modifier(Toast(message: message, trigger: trigger))
    }
}

private struct Toast<Trigger: Equatable>: ViewModifier {
    let message: LocalizedStringKey
    let trigger: Trigger

    @State private var isVisible = false
    @State private var hideTask: Task<Void, Never>?

    func body(content: Content) -> some View {
        content
            .overlay(alignment: .top) {
                if isVisible {
                    Text(message)
                        .font(.callout)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .glassEffect()
                        .padding(.top, 8)
                        .transition(.move(edge: .top).combined(with: .opacity))
                }
            }
            .onChange(of: trigger) {
                withAnimation { isVisible = true }
                hideTask?.cancel()
                hideTask = Task {
                    do {
                        try await Task.sleep(for: .seconds(2))
                    } catch {
                        return
                    }
                    withAnimation { isVisible = false }
                }
            }
            .onDisappear {
                hideTask?.cancel()
            }
    }
}
