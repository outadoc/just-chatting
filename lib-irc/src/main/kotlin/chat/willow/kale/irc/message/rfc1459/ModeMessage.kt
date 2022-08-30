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

import chat.willow.kale.IKaleParsingStateDelegate
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
import chat.willow.kale.loggerFor
import java.util.LinkedList
import java.util.Queue

object ModeMessage : ICommand {

    private val LOGGER = loggerFor<ModeMessage>()

    var parsingStateDelegate: IKaleParsingStateDelegate? = null

    override val command = "MODE"

    data class Command(val target: String, val modifiers: List<ModeModifier>) {

        object Descriptor : KaleDescriptor<Command>(matcher = commandMatcher(command), parser = Parser)

        object Parser : MessageParser<Command>() {

            override fun parseFromComponents(components: IrcMessageComponents): Command? {
                if (components.parameters.isEmpty()) {
                    return null
                }

                val target = components.parameters[0]

                if (components.parameters.size >= 2) {
                    val secondParameter = components.parameters[1]
                    if (secondParameter.isNotEmpty() && !isTokenStartOfModifier(secondParameter[0])) {
                        // special case
                        // assume that the message is for listing modes
                        //  example: MODE #meditation e

                        val modifier = ModeModifier(mode = secondParameter[0])

                        return Command(target, listOf(modifier))
                    }

                    val remainingParameters = components.parameters.subList(1, components.parameters.size)
                    val modifiers = parseIrcParameters(remainingParameters)

                    return Command(target, modifiers)
                } else {
                    return null
                }
            }
        }

        object Serialiser : MessageSerialiser<Command>(command) {

            override fun serialiseToComponents(message: Command): IrcMessageComponents {
                val parameters = serialise(message.modifiers).toMutableList()
                parameters.add(0, message.target)

                return IrcMessageComponents(parameters = parameters)
            }
        }
    }

    data class Message(val source: Prefix, val target: String, val modifiers: List<ModeModifier>) {

        object Descriptor : KaleDescriptor<Message>(matcher = commandMatcher(command), parser = Parser)

        object Parser : MessageParser<Message>() {

            override fun parseFromComponents(components: IrcMessageComponents): Message? {
                if (components.parameters.isEmpty() || components.prefix == null) {
                    return null
                }

                val source = PrefixParser.parse(components.prefix) ?: return null
                val target = components.parameters[0]

                if (components.parameters.size >= 2) {
                    val secondParameter = components.parameters[1]
                    if (secondParameter.isNotEmpty() && !isTokenStartOfModifier(secondParameter[0])) {
                        // special case
                        // assume that the message is for listing modes
                        //  example: MODE #meditation e

                        val modifier = ModeModifier(mode = secondParameter[0])

                        return Message(source, target, listOf(modifier))
                    }

                    val remainingParameters = components.parameters.subList(1, components.parameters.size)
                    val modifiers = parseIrcParameters(remainingParameters)

                    return Message(target = target, source = source, modifiers = modifiers)
                } else {
                    return null
                }
            }
        }

        object Serialiser : MessageSerialiser<Message>(command) {

            override fun serialiseToComponents(message: Message): IrcMessageComponents {
                val prefix = PrefixSerialiser.serialise(message.source)
                val parameters = serialise(message.modifiers).toMutableList()
                parameters.add(0, message.target)

                return IrcMessageComponents(prefix = prefix, parameters = parameters)
            }
        }
    }

    data class ModeModifier(val type: Char? = null, val mode: Char, var parameter: String? = null) {
        @Suppress("UNUSED") val isAdding: Boolean
            get() = this.type == CharacterCodes.PLUS

        @Suppress("UNUSED") val isRemoving: Boolean
            get() = this.type == CharacterCodes.MINUS

        @Suppress("UNUSED") val isListing: Boolean
            get() = this.type == null
    }

    private data class ModeChunk(val modes: String, val parameters: Queue<String> = LinkedList())

    private fun parseIrcParameters(parameters: List<String>): List<ModeModifier> {
        val chunks = this.parseParametersToModeChunks(parameters)
        return this.parseChunksToModifiers(chunks)
    }

    private fun isTokenStartOfModifier(token: Char?): Boolean {
        return token != null && (token == CharacterCodes.PLUS || token == CharacterCodes.MINUS)
    }

    private fun parseParametersToModeChunks(parameters: List<String>): List<ModeChunk> {
        val chunks = mutableListOf<ModeChunk>()

        if (parameters.isEmpty()) {
            return chunks
        }

        var currentChunk: ModeChunk? = null

        for (parameter in parameters) {
            if (parameter.isEmpty()) {
                LOGGER.warn("Attempted to parse an empty parameter in to a chunk - bailing")

                break
            }

            if (this.isTokenStartOfModifier(parameter[0])) {
                currentChunk = ModeChunk(parameter)
                chunks.add(currentChunk)

                continue
            }

            if (currentChunk == null) {
                LOGGER.warn("Attempted to add a chunk without having a type token first - bailing")

                break
            }

            currentChunk.parameters.add(parameter)
        }

        return chunks
    }

    private fun parseChunksToModifiers(chunks: List<ModeChunk>): List<ModeModifier> {
        val modifiers = mutableListOf<ModeModifier>()

        var currentType: Char? = null

        for ((modes, parameters) in chunks) {
            for (token in this.parseModes(modes)) {
                if (this.isTokenStartOfModifier(token)) {
                    currentType = token

                    continue
                }

                if (currentType == null) {
                    LOGGER.warn("Tried to add a modifier that didn't start with +- - bailing: '$token'")

                    continue
                }

                val modifier = ModeModifier(type = currentType, mode = token)

                val isAdding = currentType == CharacterCodes.PLUS
                val takesAParameter = this.takesAParameter(isAdding, token)

                if (takesAParameter) {
                    val parameter = parameters.poll()

                    if (parameter.isNullOrEmpty()) {
                        LOGGER.warn("MODE modifier was missing an expected parameter - not processing it: '$token'")

                        continue
                    }

                    modifier.parameter = parameter
                }

                modifiers.add(modifier)
            }

            if (!parameters.isEmpty()) {
                LOGGER.warn("Chunk had parameters left after polling - something probably went wrong!")
            }
        }

        return modifiers
    }

    private fun parseModes(token: String): List<Char> {
        return (token.indices).map { token[it] }
    }

    private fun takesAParameter(isAdding: Boolean, token: Char): Boolean {
        val delegateTakesAParameter = parsingStateDelegate?.modeTakesAParameter(isAdding, token)
        return delegateTakesAParameter ?: return if (isAdding) {
            defaultPlusRequiringParameter.contains(token)
        } else {
            defaultMinusRequiringParameter.contains(token)
        }
    }

    private fun serialise(modifiers: List<ModeModifier>): List<String> {
        val parameters = mutableListOf<String>()

        for ((type, mode, parameter) in modifiers) {
            if (type != null) {
                parameters.add("$type$mode")
            } else {
                parameters.add("$mode")
            }

            if (parameter != null) {
                parameters.add(parameter)
            }
        }

        return parameters
    }

    private val defaultPlusRequiringParameter: Set<Char> = setOf('o', 'v', 'h', 'b', 'l', 'k', 'q', 'e', 'I')
    private val defaultMinusRequiringParameter: Set<Char> = setOf('o', 'v', 'h', 'b', 'k', 'q', 'e', 'I')
}
