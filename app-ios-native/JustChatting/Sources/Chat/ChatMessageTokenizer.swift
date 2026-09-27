//
//  ChatMessageTokenizer.swift
//  JustChatting
//

import Foundation
import JCShared

enum ChatMessageToken {
    case text(String)
    case emote(Emote, overlays: [Emote])
    case mention(String)
    case link(text: String, url: URL)
}

enum ChatMessageTokenizer {
    private static let linkDetector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)

    static func tokenize(message: String?, emoteNamed: (String) -> Emote?) -> [ChatMessageToken] {
        guard let message else { return [] }
        let words = message.split(separator: " ", omittingEmptySubsequences: true).map(String.init)
        var tokens: [ChatMessageToken] = []
        var index = 0

        while index < words.count {
            let word = words[index]

            if let emote = emoteNamed(word) {
                // Collect any zero-width emotes that should overlay this one.
                var overlays: [Emote] = []
                if !emote.isZeroWidth {
                    var overlayIndex = index + 1
                    while overlayIndex < words.count,
                          let overlay = emoteNamed(words[overlayIndex]),
                          overlay.isZeroWidth {
                        overlays.append(overlay)
                        overlayIndex += 1
                    }
                }
                tokens.append(.emote(emote, overlays: overlays))
                index += 1 + overlays.count
                continue
            }

            if word.hasPrefix("@"), word.count > 1 {
                tokens.append(.mention(word))
            } else if let url = link(in: word) {
                tokens.append(.link(text: word, url: url))
            } else {
                tokens.append(.text(word))
            }
            index += 1
        }

        return tokens
    }

    private static func link(in word: String) -> URL? {
        let range = NSRange(word.startIndex..., in: word)
        guard let match = linkDetector?.firstMatch(in: word, range: range),
              match.range == range,
              let url = match.url,
              url.scheme == "http" || url.scheme == "https" else {
            return nil
        }
        return url
    }
}
