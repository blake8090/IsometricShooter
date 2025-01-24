package bke.iso.engine.ui.v2

import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.ui.util.AssetAwareSkin
import bke.iso.engine.ui.util.ControllerNavigation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.Event as GdxEvent

abstract class UILayer(protected val engine: Engine) {

    val skin: Skin = AssetAwareSkin(engine.assets)
    val stage: Stage = createStage()

    val controllerNavigation: ControllerNavigation = ControllerNavigation()

    abstract val controllers: Set<UIViewController<*>>

    abstract fun create()

    fun start() {
        for (controller in controllers) {
            controller.start()
        }
    }

    fun draw(deltaTime: Float) {
        controllers
            .filter(UIViewController<*>::enabled)
            .forEach { controller -> controller.update(deltaTime) }

        stage.act(deltaTime)
        stage.viewport.apply()
        stage.draw()
    }

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun dispose() {
        for (controller in controllers) {
            controller.stop()
        }

        stage.dispose()
        skin.dispose()
    }

    open fun handleEvent(event: Event) {
        for (controller in controllers) {
            controller.handleEvent(event)
        }
    }

    private fun createStage() = Stage(ScreenViewport()).apply {
        addListener { event ->
            handleEvent(event)
            false
        }
    }

    private fun handleEvent(event: GdxEvent) {
        for (controller in controllers) {
            // TODO: Use intermediary class to filter these events!
            controller.handleEvent(event)
        }
    }
}
