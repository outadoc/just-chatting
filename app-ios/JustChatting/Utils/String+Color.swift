//
//  String+Color.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-23.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import SwiftUI

extension String {
    func parseHex() -> Color? {
        var str: String = self

        if str.hasPrefix("#") {
            str.remove(at: str.startIndex)
        }

        if str.count != 6 {
            return nil
        }

        var rgbValue: UInt64 = 0
        Scanner(string: str).scanHexInt64(&rgbValue)

        return Color(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0
        )
    }
}
