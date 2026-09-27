//
//  User+Links.swift
//  JustChatting
//

import Foundation
import JCShared

extension User {
    /// The user's channel on the Twitch website.
    var channelUrl: URL? {
        URL(string: TwitchExtKt.createChannelExternalLink(user: self))
    }
}
