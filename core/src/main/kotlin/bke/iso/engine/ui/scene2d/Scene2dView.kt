package bke.iso.engine.ui.scene2d

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.AssetAwareSkin
import bke.iso.engine.ui.util.ControllerNavigation
import bke.iso.engine.ui.UIView
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.Event as Scene2dEvent

abstract class Scene2dView(protected val assets: Assets) : UIView() {

    val skin: Skin = AssetAwareSkin(assets)
    val stage: Stage = createStage()
    val controllerNavigation: ControllerNavigation = ControllerNavigation()

    protected abstract fun handleScene2dEvent(event: Scene2dEvent)

    override fun draw(deltaTime: Float) {
        stage.act(deltaTime)
        stage.viewport.apply()
        stage.draw()
    }

    fun dispose() {
        stage.dispose()
        skin.dispose()
    }

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    private fun createStage() = Stage(ScreenViewport()).apply {
        addListener { event ->
            handleScene2dEvent(event)
            false
        }
    }
}
