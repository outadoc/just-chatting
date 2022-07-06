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
package chat.willow.kale.irc.message.extension.monitor

import chat.willow.kale.core.ICommand
import chat.willow.kale.core.message.*
import chat.willow.kale.irc.CharacterCodes

object MonitorMessage : ICommand {

    override val command = "MONITOR"

    object Add : ISubcommand {

        override val subcommand = CharacterCodes.PLUS.toString()

        data class Command(val targets: List<String>) {

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    if (components.parameters.isEmpty()) {
                        return null
                    }

                    val rawTargets = components.parameters[0]

                    val targets = rawTargets.split(CharacterCodes.COMMA)

                    return Command(targets)
                }
            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val targets = message.targets.joinToString(separator = CharacterCodes.COMMA.toString())

                    return IrcMessageComponents(parameters = listOf(targets))
                }
            }
        }
    }

    object Clear : ISubcommand {

        override val subcommand = "C"

        object Command {

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
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

    object ListAll : ISubcommand {

        override val subcommand = "L"

        // TODO: same as Clear

        object Command {

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
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

    object Remove : ISubcommand {

        override val subcommand = CharacterCodes.MINUS.toString()

        // TODO: same as Add

        data class Command(val targets: List<String>) {

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    if (components.parameters.isEmpty()) {
                        return null
                    }

                    val rawTargets = components.parameters[0]

                    val targets = rawTargets.split(CharacterCodes.COMMA)

                    return Command(targets)
                }
            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val targets = message.targets.joinToString(separator = CharacterCodes.COMMA.toString())

                    return IrcMessageComponents(parameters = listOf(targets))
                }
            }
        }
    }

    object Status : ISubcommand {

        override val subcommand = "S"

        // TODO: same as Clear

        object Command {

            object Descriptor : KaleDescriptor<Command>(matcher = subcommandMatcher(command, subcommand, subcommandPosition = 0), parser = Parser)

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
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
}
