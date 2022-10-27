package bke.iso.v2.app

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import org.slf4j.LoggerFactory

fun configureLogback(level: Level) {
    val context: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    val consoleAppender = ConsoleAppender<ILoggingEvent>()
    consoleAppender.context = context
    consoleAppender.name = "console"
    consoleAppender.start()

    val root: Logger = context.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    root.level = level
    root.addAppender(consoleAppender)
}
