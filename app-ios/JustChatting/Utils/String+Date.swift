//
//  String+Date.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-19.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import Foundation

extension String {
    func parseDate() -> Date {
        return ISO8601DateFormatter().date(from: self)!
    }
}
