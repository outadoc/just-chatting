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

interface IMessageParser<out T> {

    fun parse(message: IrcMessage): T?
}

interface IComponentsParser<out T> {

    fun parseFromComponents(components: IrcMessageComponents): T?
}

abstract class MessageParser<out T> : IMessageParser<T>, IComponentsParser<T> {

    override fun parse(message: IrcMessage): T? {
        val components = IrcMessageComponents(tags = message.tags, prefix = message.prefix, parameters = message.parameters)

        return parseFromComponents(components)
    }
}

abstract class SubcommandParser<out T>(private val subcommand: String, private val subcommandPosition: Int = 0) : IMessageParser<T> {

    override fun parse(message: IrcMessage): T? {
        if (message.parameters.size <= subcommandPosition) {
            return null
        }

        if (message.parameters.getOrNull(subcommandPosition) != subcommand) {
            return null
        }

        val parameters = message.parameters.filterIndexed {
                index, _ ->
            index != subcommandPosition
        }

        val components = IrcMessageComponents(tags = message.tags, prefix = message.prefix, parameters = parameters)

        return parseFromComponents(components)
    }

    protected abstract fun parseFromComponents(components: IrcMessageComponents): T?
}

abstract class PrefixSubcommandParser<out T>(private val token: String, private val subcommandPosition: Int = 0) : IMessageParser<T> {

    override fun parse(message: IrcMessage): T? {
        if (message.parameters.isEmpty() || message.parameters.size <= subcommandPosition) {
            return null
        }

        val subcommandStartsWithToken = message.parameters.getOrNull(subcommandPosition)?.startsWith(token) ?: false
        if (!subcommandStartsWithToken) {
            return null
        }

        val parameters = message.parameters.toMutableList()
        parameters[subcommandPosition] = parameters[subcommandPosition].removePrefix(token)

        val components = IrcMessageComponents(tags = message.tags, prefix = message.prefix, parameters = parameters)

        return parseFromComponents(components)
    }

    protected abstract fun parseFromComponents(components: IrcMessageComponents): T?
}
