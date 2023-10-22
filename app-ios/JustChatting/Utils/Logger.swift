//
//  Logger.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-22.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared

func logDebug(tag: String, _ message: String) {
    Logger.shared.println(level: .debug, tag: tag) { message }
}

func logWarning(tag: String, _ message: String) {
    Logger.shared.println(level: .warning, tag: tag) { message }
}

func logInfo(tag: String, _ message: String) {
    Logger.shared.println(level: .info, tag: tag) { message }
}

func logError(tag: String, _ message: String, error: Error? = nil) {
    Logger.shared.println(level: .error, tag: tag) { message }

    if let error = error {
        Logger.shared.println(level: .error, tag: tag) { error.localizedDescription }
    }
}
