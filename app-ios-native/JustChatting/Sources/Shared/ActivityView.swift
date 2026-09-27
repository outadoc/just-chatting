//
//  ActivityView.swift
//  JustChatting
//

import SwiftUI
import UIKit

/// The system share sheet, for content that isn't known until after the user asked to share it,
/// which `ShareLink` can't handle.
struct ActivityView: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}
