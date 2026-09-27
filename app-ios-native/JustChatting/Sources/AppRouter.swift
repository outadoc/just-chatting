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
    case settings

    init(screen: any Screen) {
        self = switch onEnum(of: screen) {
        case .live: .live
        case .future: .schedule
        case .followed: .followed
        case .search: .search
        case .settings: .settings
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

    /// Opens a channel's chat in the current tab, or in the Live tab if the current one doesn't
    /// list channels.
    func openChannel(userId: String) {
        switch selectedTab {
        case .followed:
            followedChannelId = userId
        case .search:
            searchChannelId = userId
        case .live, .schedule, .settings:
            selectedTab = .live
            liveChannelId = userId
        }
    }
}
