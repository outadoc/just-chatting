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
package chat.willow.kale.irc.message.rfc1459.rpl

import chat.willow.kale.core.ICommand
import chat.willow.kale.core.message.IrcMessageComponents
import chat.willow.kale.core.message.KaleDescriptor
import chat.willow.kale.core.message.MessageParser
import chat.willow.kale.core.message.MessageSerialiser
import chat.willow.kale.core.message.commandMatcher
import chat.willow.kale.irc.CharacterCodes

object Rpl353Message : ICommand {

    override val command = "353"

    data class Message(val source: String, val target: String, val visibility: String, val channel: String, val names: List<String>) {

        object Descriptor : KaleDescriptor<Message>(matcher = commandMatcher(command), parser = Parser)

        object Parser : MessageParser<Message>() {

            override fun parseFromComponents(components: IrcMessageComponents): Message? {
                if (components.parameters.size < 4) {
                    return null
                }

                val source = components.prefix ?: ""
                val target = components.parameters[0]
                val visibility = components.parameters[1]
                val channel = components.parameters[2]
                val names = components.parameters[3].split(CharacterCodes.SPACE).filterNot(String::isEmpty)

                return Message(source, target, visibility, channel, names)
            }
        }

        object Serialiser : MessageSerialiser<Message>(command) {

            override fun serialiseToComponents(message: Message): IrcMessageComponents {
                val names = message.names.joinToString(separator = CharacterCodes.SPACE.toString())

                return IrcMessageComponents(prefix = message.source, parameters = listOf(message.target, message.visibility, message.channel, names))
            }
        }
    }
}
