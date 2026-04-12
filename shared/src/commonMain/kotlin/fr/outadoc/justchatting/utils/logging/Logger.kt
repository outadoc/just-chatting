package fr.outadoc.justchatting.utils.logging

public object Logger {
    public var logStrategy: LogStrategy = NoopLogStrategy

    public enum class Level(
        public val tag: String,
    ) {
        Verbose("V"),
        Debug("D"),
        Info("I"),
        Warning("W"),
        Error("E"),
    }

    public fun println(
        level: Level,
        tag: String,
        content: () -> String,
    ) {
        logStrategy.println(level, tag, content())
    }
}
