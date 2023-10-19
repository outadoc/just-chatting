//
//  AppPreferences+copy.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-19.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

extension AppPreferences {
    func doCopy(appUser: AppUser) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }

    func doCopy(showTimestamps: Bool) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }

    func doCopy(enableRecentMessages: Bool) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }

    func doCopy(enableFfzEmotes: Bool) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }

    func doCopy(enableStvEmotes: Bool) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }

    func doCopy(enableBttvEmotes: Bool) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }

    func doCopy(enablePronouns: Bool) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }

    func doCopy(enableNotifications: Bool) -> AppPreferences {
        return doCopy(
            appUser: appUser,
            showTimestamps: showTimestamps,
            enableRecentMessages: enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes,
            enableStvEmotes: enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes,
            enablePronouns: enablePronouns,
            enableNotifications: enableNotifications
        )
    }
}
