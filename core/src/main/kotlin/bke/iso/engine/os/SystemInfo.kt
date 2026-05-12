package bke.iso.engine.os

import bke.iso.engine.core.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import io.github.oshai.kotlinlogging.KotlinLogging

class SystemInfo(private val game: Game) {

    private val log = KotlinLogging.logger {}

    private val displayModes: List<Graphics.DisplayMode>
    val maxDisplayMode: Graphics.DisplayMode
    private val maxRefreshRate: Int

    val screenDensity
        get() = Gdx.graphics.density

    init {
        val modes = Gdx.graphics.displayModes
        maxRefreshRate = modes.maxOf(Graphics.DisplayMode::refreshRate)

        displayModes = modes.filter { mode -> mode.refreshRate == maxRefreshRate }
        maxDisplayMode = displayModes.maxBy { mode -> mode.width + mode.height }
    }

    fun logInfo() {
        log.info { "--- Logging system information ---" }
        log.info { "Max refresh rate: $maxRefreshRate" }
        log.info { "Supported resolutions:\n${displayModes.joinToString("\n")}" }
        log.info { "Maximum supported resolution: $maxDisplayMode" }

        log.info { "Screen PPI: ${screenDensity * 160f}" }
        log.info { "Screen PPI ratio: $screenDensity" }

        log.info { "OS name: ${System.getProperty("os.name")}" }
        log.info { "User home directory: ${game.userHomePath}" }
        log.info { "Game data path: ${game.gameDataPath}" }
    }
}
