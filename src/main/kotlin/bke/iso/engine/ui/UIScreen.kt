package bke.iso.engine.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport

abstract class UIScreen {

    protected val stage = Stage(ScreenViewport())

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
    }
}
