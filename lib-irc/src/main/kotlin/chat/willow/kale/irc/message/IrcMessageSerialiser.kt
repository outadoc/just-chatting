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
package chat.willow.kale.irc.message

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.loggerFor

interface IIrcMessageSerialiser {
    fun serialise(message: IrcMessage): String?
}

object IrcMessageSerialiser : IIrcMessageSerialiser {
    private val LOGGER = loggerFor<IrcMessageSerialiser>()

    override fun serialise(message: IrcMessage): String? {
        val builder = StringBuilder()

        if (message.tags.isNotEmpty()) {
            val tags = SerialiserHelper.serialiseKeysAndOptionalValues(message.tags, CharacterCodes.EQUALS, CharacterCodes.SEMICOLON) {
                it.replace(CharacterCodes.BACKSLASH.toString(), "\\\\")
                    .replace(CharacterCodes.SEMICOLON.toString(), "\\:")
                    .replace(CharacterCodes.SPACE.toString(), "\\s")
                    .replace(CharacterCodes.CR.toString(), "\\r")
                    .replace(CharacterCodes.LF.toString(), "\\n")
            }

            builder.append(CharacterCodes.AT)
            builder.append(tags)
            builder.append(CharacterCodes.SPACE)
        }

        if (message.prefix != null) {
            builder.append(CharacterCodes.COLON)
            builder.append(message.prefix)
            builder.append(CharacterCodes.SPACE)
        }

        builder.append(message.command)

        if (message.parameters.isNotEmpty()) {
            builder.append(CharacterCodes.SPACE)

            val parametersSize = message.parameters.size

            if (message.parameters.size > 1) {
                for (i in 0..parametersSize - 2) {
                    builder.append(message.parameters[i])
                    builder.append(CharacterCodes.SPACE)
                }
            }

            builder.append(CharacterCodes.COLON)
            builder.append(message.parameters[parametersSize - 1])
        }

        val output = builder.toString()
        if (output.length > IrcMessageParser.MAX_LINE_LENGTH) {
            LOGGER.warn("serialised message is too long: $output")
            return null
        }

        return output
    }
}

object SerialiserHelper {

    fun serialiseKeysAndOptionalValues(keyValues: Map<String, String?>, keyValueSeparator: Char, chunkSeparator: Char, valueTransform: ((String) -> (String))? = null): String {
        val serialisedKeyValues = mutableListOf<String>()

        for ((key, value) in keyValues) {
            if (value == null) {
                serialisedKeyValues.add(key)
            } else {
                if (valueTransform != null) {
                    serialisedKeyValues.add("$key$keyValueSeparator${valueTransform(value)}")
                } else {
                    serialisedKeyValues.add("$key$keyValueSeparator$value")
                }
            }
        }

        return serialisedKeyValues.joinToString(separator = chunkSeparator.toString())
    }
}
