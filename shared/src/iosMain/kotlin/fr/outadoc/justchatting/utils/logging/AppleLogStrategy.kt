package fr.outadoc.justchatting.utils.logging

import kotlinx.cinterop.ptr
import platform.darwin.OS_LOG_DEFAULT
import platform.darwin.OS_LOG_TYPE_DEBUG
import platform.darwin.OS_LOG_TYPE_DEFAULT
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_FAULT
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal

internal object AppleLogStrategy : LogStrategy {

    override fun println(level: Logger.Level, tag: String?, content: String) {
        _os_log_internal(
            dso = __dso_handle.ptr,
            log = OS_LOG_DEFAULT,
            type = when (level) {
                Logger.Level.Verbose -> OS_LOG_TYPE_DEBUG
                Logger.Level.Debug -> OS_LOG_TYPE_INFO
                Logger.Level.Info -> OS_LOG_TYPE_DEFAULT
                Logger.Level.Warning -> OS_LOG_TYPE_ERROR
                Logger.Level.Error -> OS_LOG_TYPE_FAULT
            },
            message = "[$tag] $content".replace("%", "%%"),
        )
    }
}
