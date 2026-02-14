/*
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.message.rfc1459

import fr.outadoc.justchatting.feature.chat.data.irc.parser.core.ICommand
import fr.outadoc.justchatting.feature.chat.data.irc.parser.core.message.IrcMessageComponents
import fr.outadoc.justchatting.feature.chat.data.irc.parser.core.message.MessageParser

internal object NoticeMessage : ICommand {

    override val command = "NOTICE"

    data class Command(val target: String, val message: String) {

        object Parser : MessageParser<Command>() {

            override fun parseFromComponents(components: IrcMessageComponents): Command? {
                if (components.parameters.size < 2) {
                    return null
                }

                val target = components.parameters[0]
                val privMessage = components.parameters[1]

                return Command(target, privMessage)
            }
        }
    }
}
