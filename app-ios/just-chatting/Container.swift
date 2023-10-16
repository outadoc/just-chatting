//
//  Container.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-16.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import Swinject
import shared

extension Container {
    static let shared = Container()

    func inject() {
        injectHome()
    }

    private func injectHome() {
        Container.shared.register(ChannelSearchViewModel.self) {
            r in ChannelSearchViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }
        
        Container.shared.register(FollowedChannelsViewModel.self) {
            r in FollowedChannelsViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }
        
        Container.shared.register(FollowedStreamsViewModel.self) {
            r in FollowedStreamsViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }
    }
}
