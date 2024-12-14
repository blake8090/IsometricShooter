package bke.iso.engine.render

import bke.iso.engine.core.EngineModule
import com.badlogic.gdx.Gdx

class RendererManager(private val mainRenderer: Renderer) : EngineModule() {

    override val moduleName = "renderer"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    private var activeRenderer: Renderer = mainRenderer

    override fun stop() {
        activeRenderer.stop()
    }

    override fun update(deltaTime: Float) {
        activeRenderer.draw()
    }

    fun setActiveRenderer(renderer: Renderer) {
        activeRenderer = renderer
        renderer.resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    fun reset() {
        setActiveRenderer(mainRenderer)
    }

    fun resize(width: Int, height: Int) {
        activeRenderer.resize(width, height)
    }
}
