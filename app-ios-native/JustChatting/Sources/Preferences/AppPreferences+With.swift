//
//  AppPreferences+With.swift
//  JustChatting
//

import JCShared

extension AppPreferences {
    /// Returns a copy with the given fields replaced. Kotlin's data class `copy` requires every
    /// argument from Swift, so this is the single place that lists them all.
    func with(
        showTimestamps: Bool? = nil,
        enableRecentMessages: Bool? = nil,
        enableFfzEmotes: Bool? = nil,
        enableStvEmotes: Bool? = nil,
        enableBttvEmotes: Bool? = nil,
        enablePronouns: Bool? = nil,
        enableNotifications: Bool? = nil
    ) -> AppPreferences {
        AppPreferences(
            apiToken: apiToken,
            showTimestamps: showTimestamps ?? self.showTimestamps,
            enableRecentMessages: enableRecentMessages ?? self.enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes ?? self.enableFfzEmotes,
            enableStvEmotes: enableStvEmotes ?? self.enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes ?? self.enableBttvEmotes,
            enablePronouns: enablePronouns ?? self.enablePronouns,
            enableNotifications: enableNotifications ?? self.enableNotifications
        )
    }
}
