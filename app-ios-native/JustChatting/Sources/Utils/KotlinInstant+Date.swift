//
//  KotlinInstant+Date.swift
//  JustChatting
//

import Foundation
import JCShared

extension KotlinInstant {
    /// This instant as a Foundation `Date`.
    var date: Date {
        Date(timeIntervalSince1970: Double(epochSeconds) + Double(nanosecondsOfSecond) / 1_000_000_000)
    }
}
