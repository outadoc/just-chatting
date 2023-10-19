//
//  SettingsView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct SettingsView: View {
    @State var isOn = true
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text(MR.strings.shared.settings_thirdparty_header.desc().localized())) {
                    Toggle(isOn: $isOn) {
                        Text(MR.strings.shared.settings_thirdparty_recent_title.desc().localized())
                    }
                }
            }
            .navigationTitle(Text(MR.strings.shared.settings.desc().localized()))
        }
    }
}
