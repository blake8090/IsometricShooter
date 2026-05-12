package bke.iso.engine.ui

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.input.Input
import bke.iso.engine.ui.imgui.ImGuiView
import bke.iso.engine.ui.scene2d.Scene2dView
import io.github.oshai.kotlinlogging.KotlinLogging

class UI(private val input: Input) : EngineModule() {

    private val log = KotlinLogging.logger {}

    override val moduleName = "UIv3"
    override val updateWhileLoading = true
    override val profilingEnabled = true
    override val alwaysActive: Boolean = true

    private val scene2dViews = LinkedHashSet<Scene2dView>()
    private val imGuiViews = LinkedHashSet<ImGuiView>()

    override fun update(deltaTime: Float) {
        for (view in scene2dViews) {
            view.draw(deltaTime)
        }
    }

    override fun handleEvent(event: Event) {
        for (view in scene2dViews) {
            view.handleEvent(event)
        }
        for (view in imGuiViews) {
            view.handleEvent(event)
        }
    }

    override fun stop() {
        clearScene2dViews()
        clearImGuiViews()
    }

    fun clearScene2dViews() {
        for (view in scene2dViews) {
            log.debug { "Disposing Scene2d view ${view::class.simpleName}" }
            input.removeInputProcessor(view.stage)
            input.removeControllerListener(view.controllerNavigation)
            view.dispose()
        }
        scene2dViews.clear()
    }

    fun clearImGuiViews() {
        imGuiViews.clear()
    }

    override fun onFrameEnd(deltaTime: Float) {
        for (view in imGuiViews) {
            view.draw(deltaTime)
        }
    }

    fun resize(width: Int, height: Int) {
        for (view in scene2dViews) {
            view.resize(width, height)
        }
    }

    fun pushView(view: Scene2dView) {
        log.debug { "Pushing Scene2D View '${view::class.simpleName}'" }

        input.addInputProcessor(view.stage)
        input.addControllerListener(view.controllerNavigation)
        view.create()

        if (input.isUsingController()) {
            view.controllerNavigation.start()
        }

        scene2dViews.add(view)
    }

    fun pushView(view: ImGuiView) {
        log.debug { "Pushing ImGui View '${view::class.simpleName}'" }
        view.create()
        imGuiViews.add(view)
    }

    fun removeImGuiView(view: ImGuiView) {
        log.debug { "Removing ImGui View '${view::class.simpleName}'" }
        imGuiViews.remove(view)
    }
}
