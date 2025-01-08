package bke.iso.engine.ui

import bke.iso.engine.core.Event
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.AssetAwareSkin
import bke.iso.engine.ui.util.ControllerNavigation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScreenViewport

abstract class UIScreen(protected val assets: Assets) {

    val stage: Stage = Stage(ScreenViewport())
    val controllerNavigation: ControllerNavigation = ControllerNavigation()

    protected val skin: Skin = AssetAwareSkin(assets)

    abstract fun create()

    open fun draw(deltaTime: Float) {
        stage.act(deltaTime)
        stage.viewport.apply()
        stage.draw()
    }

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun dispose() {
        stage.dispose()
        skin.dispose()
    }

    open fun handleEvent(event: Event) {}
}
