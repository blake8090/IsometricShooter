package bke.iso

import bke.iso.game.IsometricShooter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

private val log = KotlinLogging.logger {}

fun main() {
    Thread.currentThread().setUncaughtExceptionHandler { _, e ->
        log.error(e) { "Uncaught exception" }
        exitProcess(-1)
    }

    val game = IsometricShooter()
    val app = App(game)

    configureLogging("${game.gameDataPath}/main.log")

    Lwjgl3Application(app, app.getConfig())
}
