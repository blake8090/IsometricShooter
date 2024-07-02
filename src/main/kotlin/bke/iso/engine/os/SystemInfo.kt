package bke.iso.engine.os

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import mu.KotlinLogging

class SystemInfo {

    private val log = KotlinLogging.logger {}

    val displayModes: List<Graphics.DisplayMode>
    val maxDisplayMode: Graphics.DisplayMode
    val screenDensity: Float

    init {
        val modes = Gdx.graphics.displayModes
        val maxRefreshRate = modes.maxOf(Graphics.DisplayMode::refreshRate)

        displayModes = modes.filter { mode -> mode.refreshRate == maxRefreshRate }
        maxDisplayMode = displayModes.maxBy { mode -> mode.width + mode.height }

        log.info { "System info - Max refresh rate: $maxRefreshRate" }
        log.info { "System info - Supported resolutions:\n${displayModes.joinToString("\n")}" }
        log.info { "System info - Maximum supported resolution: $maxDisplayMode" }

        screenDensity = Gdx.graphics.density
        log.info { "System info - Screen PPI: ${screenDensity * 160f}" }
        log.info { "System info - Screen PPI ratio: $screenDensity" }
    }
}
