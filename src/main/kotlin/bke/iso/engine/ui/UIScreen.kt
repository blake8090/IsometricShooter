package bke.iso.engine.ui

import bke.iso.engine.Event
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport

abstract class UIScreen {

    protected val stage = Stage(ExtendViewport(2560f, 1440f))
    protected val skin = Skin()

    open fun create() {
        Gdx.input.inputProcessor = stage
    }

    fun render(deltaTime: Float) {
        stage.act(deltaTime)
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
