//
//  Date+Format.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-19.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import Foundation

extension Date {
    func formatDate() -> String? {
        let calendar = Calendar.current
        let isCurrentYear = calendar.component(.year, from: self) == calendar.component(.year, from: Date.now)

        let dateFormatter = DateFormatter()
        dateFormatter.locale = Locale.current
        dateFormatter.setLocalizedDateFormatFromTemplate(isCurrentYear ? "MMM d" : "MMM d y")
        return dateFormatter.string(from: self)
    }
}
