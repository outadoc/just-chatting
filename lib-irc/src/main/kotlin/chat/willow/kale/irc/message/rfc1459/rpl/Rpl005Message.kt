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
import chat.willow.kale.core.message.*
import chat.willow.kale.irc.CharacterCodes

object Rpl005Message : ICommand {

    override val command = "005"

    data class Message(val source: String, val target: String, val tokens: Map<String, String?>) {

        object Descriptor : KaleDescriptor<Message>(matcher = commandMatcher(command), parser = Parser)

        object Parser : MessageParser<Message>() {

            override fun parseFromComponents(components: IrcMessageComponents): Message? {
                if (components.parameters.size < 2) {
                    return null
                }

                val source = components.prefix ?: ""
                val target = components.parameters[0]

                val tokens = mutableMapOf<String, String?>()

                for (i in 1 until components.parameters.size) {
                    val token = components.parameters[i].split(CharacterCodes.EQUALS, limit = 2)

                    if (token.isEmpty() || token[0].isEmpty()) {
                        continue
                    }

                    var value = token.getOrNull(1)
                    if (value != null && value.isEmpty()) {
                        value = null
                    }

                    tokens[token[0]] = value
                }

                return Message(source, target, tokens)
            }
        }

        object Serialiser : MessageSerialiser<Message>(command) {

            override fun serialiseToComponents(message: Message): IrcMessageComponents {
                val tokens = mutableListOf<String>()

                for ((key, value) in message.tokens) {
                    if (value.isNullOrEmpty()) {
                        tokens.add(key)
                    } else {
                        tokens.add("$key${CharacterCodes.EQUALS}$value")
                    }
                }

                val parameters = listOf(message.target) + tokens

                return IrcMessageComponents(prefix = message.source, parameters = parameters)
            }
        }
    }
}
