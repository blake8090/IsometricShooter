package bke.iso

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import org.slf4j.LoggerFactory

class DynamicFileAppender : FileAppender<ILoggingEvent>() {

    fun updateFile(path: String) {
        file = path
        stop()
        start()
    }
}

fun configureLogging(logFilePath: String) {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val root: Logger = context.getLogger(Logger.ROOT_LOGGER_NAME)

    val dynamicFileAppender = root.getAppender("file") as DynamicFileAppender
    dynamicFileAppender.updateFile(logFilePath)
}
