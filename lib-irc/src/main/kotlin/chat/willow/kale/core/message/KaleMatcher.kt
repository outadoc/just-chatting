/*
 * Copyright © 2016, Sky Welch <license@bunnies.io>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package chat.willow.kale.core.message

import chat.willow.kale.core.tag.ITagStore

typealias KaleMatcher = (IrcMessage) -> Boolean

fun commandMatcher(command: String): KaleMatcher =
    { it.command.equals(command, ignoreCase = true) }

fun subcommandMatcher(command: String, subcommand: String, subcommandPosition: Int = 1): KaleMatcher =
    { it.command.equals(command, ignoreCase = true) && it.parameters.getOrNull(subcommandPosition)?.equals(subcommand, ignoreCase = true) ?: false }

open class KaleDescriptor<out T>(val matcher: KaleMatcher, val parser: IMessageParser<T>)

data class KaleObservable<out T>(val message: T, val meta: ITagStore)
