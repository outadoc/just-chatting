package fr.outadoc.justchatting.component.ircparser.irc.message.rfc1459

import fr.outadoc.justchatting.component.ircparser.core.ICommand
import fr.outadoc.justchatting.component.ircparser.core.message.IrcMessageComponents
import fr.outadoc.justchatting.component.ircparser.core.message.MessageParser

object NoticeMessage : ICommand {

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
