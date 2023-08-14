package bke.iso.engine.ui

import bke.iso.engine.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScreenViewport

abstract class UIScreen {

    val stage = Stage(ScreenViewport())
    val controllerNavigation = ControllerNavigation()

    protected val skin = Skin()

    abstract fun create()

    fun render(deltaTime: Float) {
        stage.act(deltaTime)
        stage.viewport.apply()
        stage.draw()
    }

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    open fun dispose() {
        stage.dispose()
        skin.dispose()
    }

    open fun handleEvent(event: Event) {}
}
