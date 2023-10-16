//
//  HomeView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-16.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import SwiftUI
import JCShared

struct HomeView: View {

    let viewModel: MainRouterViewModel
    init(viewModel: MainRouterViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        VStack {
            Text(
                MR.strings().onboarding_title.format(args_: [MR.strings().app_name.desc()]).localized()
            )
        }
    }
}

