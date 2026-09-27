//
//  EmoteUrls+ColorScheme.swift
//  JustChatting
//

import JCShared
import SwiftUI

extension EmoteUrls {
    func url(for colorScheme: ColorScheme) -> URL? {
        let dict = colorScheme == .dark ? dark : light
        for scale: Float in [2.0, 1.0, 4.0] {
            if let urlStr = dict[KotlinFloat(value: scale)] as? String {
                return URL(string: urlStr)
            }
        }
        return dict.values.first.flatMap { URL(string: $0) }
    }
}
