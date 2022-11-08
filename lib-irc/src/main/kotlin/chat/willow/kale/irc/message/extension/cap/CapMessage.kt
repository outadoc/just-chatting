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
package chat.willow.kale.irc.message.extension.cap

import chat.willow.kale.core.ICommand
import chat.willow.kale.core.message.IMessageParser
import chat.willow.kale.core.message.ISubcommand
import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.core.message.IrcMessageComponents
import chat.willow.kale.core.message.KaleDescriptor
import chat.willow.kale.core.message.SubcommandParser
import chat.willow.kale.core.message.SubcommandSerialiser
import chat.willow.kale.core.message.subcommandMatcher
import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.message.ParseHelper
import chat.willow.kale.irc.message.SerialiserHelper
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.prefix.PrefixParser
import chat.willow.kale.irc.prefix.PrefixSerialiser

object CapMessage : ICommand {

    override val command = "CAP"

    object Parser : IMessageParser<CapMessage> {

        override fun parse(message: IrcMessage): CapMessage? {
            TODO("not implemented")
        }
    }

    object Ls : ISubcommand {

        override val subcommand = "LS"

        // CAP LS <version>

        data class Command(val version: String?) {

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command {
                    val version = components.parameters.getOrNull(0)

                    return Command(version)
                }
            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val parameters: List<String> = if (message.version == null) {
                        listOf()
                    } else {
                        listOf(message.version)
                    }

                    return IrcMessageComponents(parameters = parameters)
                }
            }
        }

        data class Message(val source: Prefix?, val target: String, val caps: Map<String, String?>, val isMultiline: Boolean = false) {

            // CAP * LS ...

            object Descriptor : KaleDescriptor<Message>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 1), parser = Parser)

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val source = PrefixParser.parse(components.prefix ?: "")
                    val target = components.parameters[0]
                    val asteriskOrCaps = components.parameters[1]

                    val rawCaps: String
                    val isMultiline: Boolean

                    if (asteriskOrCaps == "*") {
                        rawCaps = components.parameters.getOrNull(2) ?: ""
                        isMultiline = true
                    } else {
                        rawCaps = asteriskOrCaps
                        isMultiline = false
                    }

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS)

                    return Message(source, target, caps, isMultiline)
                }
            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(message.caps, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return if (message.source != null) {
                        IrcMessageComponents(prefix = PrefixSerialiser.serialise(message.source), parameters = listOf(message.target, caps))
                    } else {
                        IrcMessageComponents(parameters = listOf(message.target, caps))
                    }
                }
            }
        }
    }

    object Ack : ISubcommand {

        override val subcommand = "ACK"

        data class Command(val caps: List<String>) {

            // CAP ACK :caps

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    if (components.parameters.isEmpty()) {
                        return null
                    }

                    val rawCaps = components.parameters[0]

                    val caps = rawCaps.split(CharacterCodes.SPACE).filterNot(String::isEmpty)

                    return Command(caps)
                }
            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val caps = message.caps.joinToString(separator = " ")

                    val parameters = listOf(caps)

                    return IrcMessageComponents(parameters)
                }
            }
        }

        data class Message(val source: Prefix?, val target: String, val caps: List<String>) {

            // CAP * ACK :
            object Descriptor : KaleDescriptor<Message>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 1), parser = Parser)

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val prefix = PrefixParser.parse(components.prefix ?: "")
                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = rawCaps.split(CharacterCodes.SPACE).filterNot(String::isEmpty)

                    return Message(prefix, target, caps)
                }
            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val caps = message.caps.joinToString(separator = " ")

                    val parameters = listOf(message.target, caps)

                    return if (message.source != null) {
                        IrcMessageComponents(prefix = PrefixSerialiser.serialise(message.source), parameters = parameters)
                    } else {
                        IrcMessageComponents(parameters)
                    }
                }
            }
        }
    }

    object Del : ISubcommand {

        override val subcommand = "DEL"

        data class Message(val target: String, val caps: List<String>) {

            // CAP * DEL :

            object Descriptor : KaleDescriptor<Message>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 1), parser = Parser)

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS).keys.toList()

                    return Message(target, caps)
                }
            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val capsToValues = message.caps.associate { (it to null) }
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(capsToValues, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return IrcMessageComponents(parameters = listOf(message.target, caps))
                }
            }
        }
    }

    object End : ISubcommand {

        override val subcommand = "END"

        object Command {

            // CAP END

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command {
                    return Command
                }
            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    return IrcMessageComponents()
                }
            }
        }
    }

    object Nak : ISubcommand {

        override val subcommand = "NAK"

        data class Message(val source: Prefix?, val target: String, val caps: List<String>) {

            // CAP * NAK :
            // TODO: Same as DEL

            object Descriptor : KaleDescriptor<Message>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 1), parser = Parser)

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val prefix = PrefixParser.parse(components.prefix ?: "")
                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS).keys.toList()

                    return Message(prefix, target, caps)
                }
            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val capsToValues = message.caps.associate { (it to null) }
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(capsToValues, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return if (message.source != null) {
                        IrcMessageComponents(prefix = PrefixSerialiser.serialise(message.source), parameters = listOf(message.target, caps))
                    } else {
                        IrcMessageComponents(parameters = listOf(message.target, caps))
                    }
                }
            }
        }
    }

    object New : ISubcommand {

        override val subcommand = "NEW"

        data class Message(val source: Prefix?, val target: String, val caps: Map<String, String?>) {

            // CAP * NEW :
            // TODO: Same as DEL

            object Descriptor : KaleDescriptor<Message>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 1), parser = Parser)

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val prefix = PrefixParser.parse(components.prefix ?: "")
                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS)

                    return Message(prefix, target, caps)
                }
            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(message.caps, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return if (message.source != null) {
                        IrcMessageComponents(prefix = PrefixSerialiser.serialise(message.source), parameters = listOf(message.target, caps))
                    } else {
                        IrcMessageComponents(parameters = listOf(message.target, caps))
                    }
                }
            }
        }
    }

    object Req : ISubcommand {

        override val subcommand = "REQ"

        data class Command(val caps: List<String>) {

            // CAP REQ :
            // TODO: Same as ACK

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    if (components.parameters.isEmpty()) {
                        return null
                    }

                    val rawCaps = components.parameters[0]

                    val caps = rawCaps.split(CharacterCodes.SPACE).filterNot(String::isEmpty)

                    return Command(caps)
                }
            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val caps = message.caps.joinToString(separator = " ")

                    val parameters = listOf(caps)

                    return IrcMessageComponents(parameters)
                }
            }
        }
    }
}
