//
//  HomeView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-16.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct HomeView: View {
    let viewModel: MainRouterViewModel
    init(viewModel: MainRouterViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        VStack(spacing: 16) {
            Text(MR.strings().onboarding_title.format(args_: [MR.strings().app_name.desc()]).localized())
                .font(.title)

            Text(MR.strings().onboarding_message.desc().localized())
                .font(.subheadline)

            Button(MR.strings().onboarding_login_action.desc().localized()) {
                viewModel.onLoginClick()
            }
        }
        .padding(.all)
    }
}
