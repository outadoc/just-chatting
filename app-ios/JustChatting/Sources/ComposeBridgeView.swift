//
//  ComposeBridgeView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 16/09/2024.
//  Copyright © 2024 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import UIKit

struct ComposeBridgeView: UIViewControllerRepresentable {
    var onShowAuthPage: (URL) -> Void

    init(onShowAuthPage: @escaping (URL) -> Void) {
        self.onShowAuthPage = onShowAuthPage
    }

    func makeUIViewController(context _: Context) -> UIViewController {
        MainViewControllerKt.getMainViewController(
            onShowAuthPage: onShowAuthPage
        )
    }

    func updateUIViewController(_: UIViewController, context _: Context) {}
}
