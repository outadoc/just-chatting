//
//  AppRouter.swift
//  JustChatting
//

import JCShared
import Observation

enum AppTab: String, Hashable {
    case live
    case schedule
    case followed
    case search

    /// The tab showing `screen`, or nil for screens that aren't tabs, like the settings.
    init?(screen: any Screen) {
        switch onEnum(of: screen) {
        case .live: self = .live
        case .future: self = .schedule
        case .followed: self = .followed
        case .search: self = .search
        case .settings: return nil
        }
    }
}

/// Navigation state of the logged-in UI. Each channel list tab keeps its own selected chat, so
/// that switching tabs doesn't lose the conversation that was open in the other one.
@Observable
final class AppRouter {
    var selectedTab: AppTab = .live
    var liveChannelId: String?
    var followedChannelId: String?
    var searchChannelId: String?
    /// The settings are a sheet, opened from the profile button of any tab.
    var isSettingsPresented = false

    /// Shows `screen`, whether it's a tab or the settings.
    func navigate(to screen: any Screen) {
        if let tab = AppTab(screen: screen) {
            selectedTab = tab
        } else {
            isSettingsPresented = true
        }
    }

    /// Opens a channel's chat in the current tab, or in the Live tab if the current one doesn't
    /// list channels.
    func openChannel(userId: String) {
        switch selectedTab {
        case .followed:
            followedChannelId = userId
        case .search:
            searchChannelId = userId
        case .live, .schedule:
            selectedTab = .live
            liveChannelId = userId
        }
    }
}
