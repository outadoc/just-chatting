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
package chat.willow.kale.core

import chat.willow.kale.core.message.*

object RplSourceTargetChannelContent {

    open class Message(val source: String, val target: String, val channel: String, val content: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Message) return false

            if (source != other.source) return false
            if (target != other.target) return false
            if (channel != other.channel) return false
            if (content != other.content) return false

            return true
        }

        override fun hashCode(): Int {
            var result = source.hashCode()
            result = 31 * result + target.hashCode()
            result = 31 * result + channel.hashCode()
            result = 31 * result + content.hashCode()
            return result
        }
    }

    abstract class Parser(val command: String) : MessageParser<Message>() {

        override fun parseFromComponents(components: IrcMessageComponents): Message? {
            if (components.parameters.size < 3) {
                return null
            }

            val source = components.prefix ?: ""
            val target = components.parameters[0]
            val channel = components.parameters[1]
            val content = components.parameters[2]

            return Message(source, target, channel, content)
        }
    }

    abstract class Serialiser(val command: String) : MessageSerialiser<Message>(command) {

        override fun serialiseToComponents(message: Message): IrcMessageComponents {
            return IrcMessageComponents(prefix = message.source, parameters = listOf(message.target, message.channel, message.content))
        }
    }

    abstract class Descriptor(command: String, parser: IMessageParser<Message>) : KaleDescriptor<Message>(matcher = commandMatcher(command), parser = parser)
}
