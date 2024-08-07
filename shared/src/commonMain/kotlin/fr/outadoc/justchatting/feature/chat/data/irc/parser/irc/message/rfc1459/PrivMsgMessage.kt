/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.message.rfc1459

import fr.outadoc.justchatting.feature.chat.data.irc.parser.core.ICommand
import fr.outadoc.justchatting.feature.chat.data.irc.parser.core.message.IrcMessageComponents
import fr.outadoc.justchatting.feature.chat.data.irc.parser.core.message.MessageParser
import fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.prefix.Prefix
import fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.prefix.PrefixParser

internal object PrivMsgMessage : ICommand {

    override val command = "PRIVMSG"

    data class Message(val source: Prefix, val target: String, val message: String) {

        object Parser : MessageParser<Message>() {

            override fun parseFromComponents(components: IrcMessageComponents): Message? {
                if (components.parameters.size < 2) {
                    return null
                }

                val source = PrefixParser.parse(components.prefix ?: "") ?: return null
                val target = components.parameters[0]
                val message = components.parameters[1]

                return Message(source, target, message)
            }
        }
    }
}
