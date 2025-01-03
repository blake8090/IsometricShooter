package bke.iso.engine.ui

import bke.iso.engine.core.Event
import bke.iso.engine.core.EngineModule
import bke.iso.engine.input.Input
import com.badlogic.gdx.utils.Array
import io.github.oshai.kotlinlogging.KotlinLogging

class UI(private val input: Input) : EngineModule() {

    private val log = KotlinLogging.logger {}

    override val moduleName = "ui"
    override val updateWhileLoading = true
    override val profilingEnabled = true

    private val screens = Array<UIScreen>()

    override fun update(deltaTime: Float) {
        for (screen in screens) {
            screen.draw(deltaTime)
        }
    }

    override fun handleEvent(event: Event) {
        for (screen in screens) {
            screen.handleEvent(event)
        }
    }

    override fun stop() {
        for (screen in screens) {
            log.debug { "Disposing screen ${screen::class.simpleName}" }
            input.removeInputProcessor(screen.stage)
            input.removeControllerListener(screen.controllerNavigation)
            screen.dispose()
        }
        screens.clear()
    }

    fun resize(width: Int, height: Int) {
        for (screen in screens) {
            screen.resize(width, height)
        }
    }

    fun setScreen(screen: UIScreen) {
        clear()
        log.debug { "Setting screen to ${screen::class.simpleName}" }
        addScreen(screen)
    }

    fun addScreen(screen: UIScreen) {
        log.debug { "Adding screen ${screen::class.simpleName}" }

        screens.add(screen)
        input.addInputProcessor(screen.stage)
        input.addControllerListener(screen.controllerNavigation)
        screen.create()

        if (input.isUsingController()) {
            screen.controllerNavigation.start()
        }
    }

    fun dispose() {
        clear()
    }

    private fun clear() {
        for (screen in screens) {
            log.debug { "Disposing screen ${screen::class.simpleName}" }
            input.removeInputProcessor(screen.stage)
            input.removeControllerListener(screen.controllerNavigation)
            screen.dispose()
        }
        screens.clear()
    }
}
