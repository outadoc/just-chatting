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
package chat.willow.kale.irc.message.rfc1459

import chat.willow.kale.core.ICommand
import chat.willow.kale.core.message.IrcMessageComponents
import chat.willow.kale.core.message.KaleDescriptor
import chat.willow.kale.core.message.MessageParser
import chat.willow.kale.core.message.MessageSerialiser
import chat.willow.kale.core.message.commandMatcher
import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.prefix.PrefixParser
import chat.willow.kale.irc.prefix.PrefixSerialiser

object KickMessage : ICommand {

    override val command = "KICK"

    data class Command(val users: List<String>, val channels: List<String>, val comment: String? = null) {

        object Descriptor : KaleDescriptor<Command>(matcher = commandMatcher(command), parser = Parser)

        object Parser : MessageParser<Command>() {

            override fun parseFromComponents(components: IrcMessageComponents): Command? {
                if (components.parameters.size < 2) {
                    return null
                }

                val channels = components.parameters[0].split(CharacterCodes.COMMA)
                val users = components.parameters[1].split(CharacterCodes.COMMA)

                if (channels.isEmpty() || channels.size != users.size) {
                    return null
                }

                val comment = components.parameters.getOrNull(2)

                return Command(users = users, channels = channels, comment = comment)
            }
        }

        object Serialiser : MessageSerialiser<Command>(command) {

            override fun serialiseToComponents(message: Command): IrcMessageComponents {
                val channels = message.channels.joinToString(separator = CharacterCodes.COMMA.toString())
                val users = message.users.joinToString(separator = CharacterCodes.COMMA.toString())
                val comment = message.comment

                return if (comment != null) {
                    IrcMessageComponents(parameters = listOf(channels, users, comment))
                } else {
                    IrcMessageComponents(parameters = listOf(channels, users))
                }
            }
        }
    }

    data class Message(val source: Prefix, val users: List<String>, val channels: List<String>, val comment: String? = null) {

        object Descriptor : KaleDescriptor<Message>(matcher = commandMatcher(command), parser = Parser)

        object Parser : MessageParser<Message>() {

            override fun parseFromComponents(components: IrcMessageComponents): Message? {
                if (components.parameters.size < 2) {
                    return null
                }

                val source = PrefixParser.parse(components.prefix ?: "") ?: return null
                val channels = components.parameters[0].split(CharacterCodes.COMMA)
                val users = components.parameters[1].split(CharacterCodes.COMMA)

                if (channels.isEmpty() || channels.size != users.size) {
                    return null
                }

                val comment = components.parameters.getOrNull(2)

                return Message(source = source, users = users, channels = channels, comment = comment)
            }
        }

        object Serialiser : MessageSerialiser<Message>(command) {

            override fun serialiseToComponents(message: Message): IrcMessageComponents {
                val prefix = PrefixSerialiser.serialise(message.source)
                val channels = message.channels.joinToString(separator = CharacterCodes.COMMA.toString())
                val users = message.users.joinToString(separator = CharacterCodes.COMMA.toString())
                val comment = message.comment

                return if (comment != null) {
                    IrcMessageComponents(prefix = prefix, parameters = listOf(channels, users, comment))
                } else {
                    IrcMessageComponents(prefix = prefix, parameters = listOf(channels, users))
                }
            }
        }
    }
}
