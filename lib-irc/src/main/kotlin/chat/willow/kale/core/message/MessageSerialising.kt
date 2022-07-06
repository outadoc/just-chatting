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

interface IMessageSerialiser<in T> {

    fun serialise(message: T): IrcMessage?
}

interface IComponentsSerialiser<in T> {

    fun serialiseToComponents(message: T): IrcMessageComponents
}

abstract class MessageSerialiser<in T>(private val command: String) : IMessageSerialiser<T>, IComponentsSerialiser<T> {

    override fun serialise(message: T): IrcMessage? {
        val components = serialiseToComponents(message)

        return IrcMessage(command = command, tags = components.tags, prefix = components.prefix, parameters = components.parameters)
    }
}

abstract class SubcommandSerialiser<in T>(private val command: String, private val subcommand: String, private val subcommandPosition: Int = 0) : IMessageSerialiser<T> {

    override fun serialise(message: T): IrcMessage? {
        val components = serialiseToComponents(message)

        val parameters = components.parameters.toMutableList()
        parameters.add(subcommandPosition, subcommand)

        return IrcMessage(command = command, tags = components.tags, prefix = components.prefix, parameters = parameters)
    }

    protected abstract fun serialiseToComponents(message: T): IrcMessageComponents
}

abstract class PrefixSubcommandSerialiser<in T>(private val command: String, private val token: String, private val subcommandPosition: Int = 0) : IMessageSerialiser<T> {

    override fun serialise(message: T): IrcMessage? {
        val components = serialiseToComponents(message)

        val parameters = components.parameters.toMutableList()
        parameters[subcommandPosition] = token + parameters[subcommandPosition]

        return IrcMessage(command = command, tags = components.tags, prefix = components.prefix, parameters = parameters)
    }

    protected abstract fun serialiseToComponents(message: T): IrcMessageComponents
}
