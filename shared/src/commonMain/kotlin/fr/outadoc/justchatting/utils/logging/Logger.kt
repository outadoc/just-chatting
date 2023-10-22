package fr.outadoc.justchatting.utils.logging

object Logger {

    var logStrategy: LogStrategy = NoopLogStrategy

    enum class Level(val tag: String) {
        Verbose("V"),
        Debug("D"),
        Info("I"),
        Warning("W"),
        Error("E"),
    }

    fun println(level: Level, tag: String, content: () -> String) {
        logStrategy.println(level, tag, content())
    }
}
