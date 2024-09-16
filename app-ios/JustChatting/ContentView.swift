//
//  ContentView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 13/09/2024.
//  Copyright © 2024 Baptiste Candellier. All rights reserved.
//

import UIKit
import SwiftUI
import JCShared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.getMainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct MainView: View {

    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
