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
package chat.willow.kale.irc.message.extension.batch

import chat.willow.kale.core.ICommand
import chat.willow.kale.core.message.*
import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.prefix.PrefixParser
import chat.willow.kale.irc.prefix.PrefixSerialiser

object BatchMessage : ICommand {

    override val command = "BATCH"

    object Start : ISubcommand {

        override val subcommand = CharacterCodes.PLUS.toString()

        data class Message(val source: Prefix, val reference: String, val type: String, val parameters: List<String> = listOf()) {

            // BATCH +something
            // todo: descriptor

            object Parser : PrefixSubcommandParser<Message>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val source = PrefixParser.parse(components.prefix ?: "") ?: return null
                    val reference = components.parameters[0]
                    val type = components.parameters[1]
                    val parameters = components.parameters.drop(2)

                    return Message(source, reference, type, parameters)
                }
            }

            object Serialiser : PrefixSubcommandSerialiser<Message>(command, subcommand) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val parameters = listOf(message.reference, message.type) + message.parameters

                    val prefix = PrefixSerialiser.serialise(message.source)

                    return IrcMessageComponents(prefix = prefix, parameters = parameters)
                }
            }
        }
    }

    object End : ISubcommand {

        override val subcommand = CharacterCodes.MINUS.toString()

        data class Message(val source: Prefix, val reference: String) {

            // BATCH -something
            // todo: descriptor

            object Parser : PrefixSubcommandParser<Message>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.isEmpty()) {
                        return null
                    }

                    val source = PrefixParser.parse(components.prefix ?: "") ?: return null
                    val reference = components.parameters[0]

                    return Message(source, reference)
                }
            }

            object Serialiser : PrefixSubcommandSerialiser<Message>(command, subcommand) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val prefix = PrefixSerialiser.serialise(message.source)
                    val parameters = listOf(message.reference)

                    return IrcMessageComponents(prefix = prefix, parameters = parameters)
                }
            }
        }
    }
}
