//
//  PillView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import SwiftUI

struct PillView: View {
    var text: String
    var body: some View {
        Text("(\(text))")
    }
}

struct PillView_Previews: PreviewProvider {
    static var previews: some View {
        PillView(text: "Français")
    }
}
