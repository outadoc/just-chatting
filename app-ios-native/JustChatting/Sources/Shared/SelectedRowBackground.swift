//
//  SelectedRowBackground.swift
//  JustChatting
//

import SwiftUI

extension View {
    /// Highlights the selected row of a channel list in gray, whether or not the list has focus.
    ///
    /// By default, the selected row of a focused list is filled with the accent color, which the
    /// row's tinted content doesn't stand out from. The gray matches the default highlight of a
    /// selected row in a list that doesn't have focus.
    func selectedRowBackground(id: String, selection: String?) -> some View {
        let background: Color? = id == selection ? Color(uiColor: .systemGray4) : nil
        return listRowBackground(background)
    }
}
