//
//  Date+Epoch.swift
//  JustChatting
//

import Foundation

extension Date {
    /// Builds a Date from the components of a Kotlin `Instant`.
    init(epochSeconds: Int64, nanosecondsOfSecond: Int32) {
        self.init(
            timeIntervalSince1970: Double(epochSeconds)
                + Double(nanosecondsOfSecond) / 1_000_000_000
        )
    }
}
