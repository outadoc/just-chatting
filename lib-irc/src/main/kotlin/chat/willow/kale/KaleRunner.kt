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
package chat.willow.kale

import chat.willow.kale.core.tag.KaleTagRouter
import chat.willow.kale.irc.message.extension.batch.BatchMessage
import chat.willow.kale.irc.message.rfc1459.*
import chat.willow.kale.irc.prefix.Prefix

object KaleRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello, Kale!")

        val kale = Kale(KaleClientRouter(), KaleMetadataFactory(KaleTagRouter()))

        kale.observe(UserMessage.Command.Descriptor)
            .subscribe { println("USER message: $it") }

        kale.observe(NickMessage.Command.Descriptor)
            .subscribe { println("NICK message: $it") }

        kale.lines.onNext("NICK nickname")
        kale.lines.onNext("USER username mode * realname")

        kale.lines.onNext("@tag;key=value :user!host@server WHATEVER :more stuff")
        kale.lines.onNext("PING :token")
        kale.lines.onNext("PONG :token2")
        kale.lines.onNext("QUIT :")
        kale.lines.onNext("PASS :password")
        kale.lines.onNext("JOIN #channel,#channel2 key1")
        kale.lines.onNext(":nick JOIN channel account :real name")
        kale.lines.onNext("PART #channel,#channel2")
        kale.lines.onNext("MODE &oulu +b *!*@*.edu -e *!*@*.bu.edu")
        kale.lines.onNext("PRIVMSG #mychannel :this is a message! ")
        kale.lines.onNext("NOTICE #mychannel :this is a notice! ")
        kale.lines.onNext(":someone INVITE user #channel")
        kale.lines.onNext(":someone TOPIC #channel :a topic!")
        kale.lines.onNext("KICK #channel1,#channel2 user1,user2 :kicked!")
        kale.lines.onNext(":test.server 001 testnickname :welcome to test server!")
        kale.lines.onNext(":test.server 002 testnickname :your host is imaginary.server, running version x")
        kale.lines.onNext(":test.server 003 testnickname :this server was created date")
        kale.lines.onNext(":test.server 005 testnickname KEY=VALUE KEY2= KEY3=\uD83D\uDC30")
        kale.lines.onNext(":test.server 331 #channel :no topic is set")
        kale.lines.onNext(":test.server 332 testnickname #channel :channel topic!")
        kale.lines.onNext(":test.server 353 testnickname @ #channel :testnickname @another-nick")
        kale.lines.onNext(":test.server 372 testnickname :- MOTD content")
        kale.lines.onNext(":test.server 375 testnickname :- test.server Message of the day - ")
        kale.lines.onNext(":test.server 376 testnickname :End of MOTD command")
        kale.lines.onNext(":test.server 422 testnickname :MOTD File is missing")
        kale.lines.onNext("BATCH +reference type param1")
        kale.lines.onNext("BATCH -reference")
        kale.lines.onNext(":test.server 903 testnickname :message")

        kale.lines.onNext("@account=testuser;time=2012-06-30T23:59:60.419Z PRIVMSG #mychannel :this is a message! ")

        println(kale.serialise(PingMessage.Command(token = "token")))
        println(kale.serialise(PongMessage.Message(token = "token2")))
        println(kale.serialise(NickMessage.Command(nickname = "nickname")))
        println(kale.serialise(UserMessage.Command(username = "username", mode = "mode", realname = "realname")))
        println(kale.serialise(QuitMessage.Command(message = "")))
        println(kale.serialise(PassMessage.Command(password = "password")))
        println(kale.serialise(JoinMessage.Command(channels = listOf("#channel", "#channel2"), keys = listOf("key1"))))
        println(kale.serialise(PartMessage.Command(channels = listOf("#channel", "#channel2"))))
        println(kale.serialise(ModeMessage.Command(target = "#channel", modifiers = listOf(ModeMessage.ModeModifier(type = '+', mode = 'b', parameter = "somebody")))))
        println(kale.serialise(PrivMsgMessage.Command(target = "person", message = "hello")))
        println(kale.serialise(PrivMsgMessage.Command(target = "#channel", message = "I am a bot")))
        println(kale.serialise(InviteMessage.Message(source = Prefix(nick = "someone"), user = "user", channel = "#channel")))
        println(kale.serialise(TopicMessage.Message(source = Prefix(nick = "someone"), channel = "#channel", topic = "a topic!")))
        println(kale.serialise(KickMessage.Command(channels = listOf("#channel1", "#channel2"), users = listOf("user1", "user2"), comment = "kicked!")))
        println(kale.serialise(BatchMessage.Start.Message(source = Prefix(nick = "someone"), reference = "reference", type = "type", parameters = listOf("parameter1", "parameter2"))))
        println(kale.serialise(BatchMessage.End.Message(source = Prefix(nick = "someone"), reference = "reference")))
    }
}
