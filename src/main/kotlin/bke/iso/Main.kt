package bke.iso

//import bke.iso.app.App
//import bke.iso.game.IsometricShooterGame
import bke.iso.game.IsometricShooter
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import org.slf4j.LoggerFactory

val LOG_LEVEL: Level = Level.TRACE

fun main() {
    TODO("fix false positive with circular dependency")
//    setupLogging()
//    val app = App(IsometricShooterGame::class)
//    val app = App("Isometric Shooter", IsometricShooter::class)
//    Lwjgl3Application(app, app.buildConfig())
}

private fun setupLogging() {
    val context: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    val consoleAppender = ConsoleAppender<ILoggingEvent>().apply {
        this.context = context
        name = "console"
        start()
    }

    (context.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).apply {
        level = LOG_LEVEL
        addAppender(consoleAppender)
    }
}
