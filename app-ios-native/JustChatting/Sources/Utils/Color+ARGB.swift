//
//  Color+ARGB.swift
//  JustChatting
//

import SwiftUI
import UIKit

// The shared Kotlin code handles colors as ARGB ints.

extension Color {
    init(argb: Int32) {
        let value = UInt32(bitPattern: argb)
        self.init(
            .sRGB,
            red: Double((value >> 16) & 0xFF) / 255,
            green: Double((value >> 8) & 0xFF) / 255,
            blue: Double(value & 0xFF) / 255,
            opacity: Double((value >> 24) & 0xFF) / 255
        )
    }
}

extension UIColor {
    /// This color as it appears in `colorScheme`, as an ARGB int.
    func argb(in colorScheme: ColorScheme) -> Int32 {
        let traits = UITraitCollection(userInterfaceStyle: colorScheme == .dark ? .dark : .light)
        var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 0
        resolvedColor(with: traits).getRed(&red, green: &green, blue: &blue, alpha: &alpha)

        func component(_ value: CGFloat) -> UInt32 {
            UInt32((min(max(value, 0), 1) * 255).rounded())
        }

        let value = component(alpha) << 24 | component(red) << 16 | component(green) << 8 | component(blue)
        return Int32(bitPattern: value)
    }
}
