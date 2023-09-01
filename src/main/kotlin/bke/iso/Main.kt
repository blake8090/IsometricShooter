package bke.iso

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

fun main() {
    Thread.currentThread().setUncaughtExceptionHandler { t, e ->
        log.error(e) { "Uncaught exception" }
        System.exit(-1)
    }

    val app = App("Isometric Shooter")
    Lwjgl3Application(app, app.config)
}
