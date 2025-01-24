package bke.iso.engine.ui.v2

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.input.Input
import io.github.oshai.kotlinlogging.KotlinLogging

class UI(private val input: Input) : EngineModule() {

    private val log = KotlinLogging.logger {}

    override val moduleName = "ui2"
    override val updateWhileLoading = true
    override val profilingEnabled = true

    private var layer: UILayer? = null

    override fun stop() {
        layer?.dispose()
    }

    override fun update(deltaTime: Float) {
        layer?.draw(deltaTime)
    }

    override fun handleEvent(event: Event) {
        layer?.handleEvent(event)
    }

    fun setLayer(layer: UILayer) {
        log.debug { "Setting layer ${layer::class.simpleName}" }

        input.addInputProcessor(layer.stage)
        input.addControllerListener(layer.controllerNavigation)
        layer.create()

        if (input.isUsingController()) {
            layer.controllerNavigation.start()
        }

        this.layer = layer
    }

    fun resize(width: Int, height: Int) {
        layer?.resize(width, height)
    }
}
