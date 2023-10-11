package fr.outadoc.justchatting.utils.logging

import platform.Foundation.NSLog

object AppleLogStrategy : LogStrategy {

    override fun println(level: Logger.Level, tag: String?, content: String) {
        NSLog("[${level.value}] $tag: $content")
    }
}
