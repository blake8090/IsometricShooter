package bke.iso.editor.v2.actor

import bke.iso.editor.v2.core.EditorViewController
import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Events
import bke.iso.engine.core.Module
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class ActorTabViewController(
    skin: Skin,
    assets: Assets,
    events: Events,
) : EditorViewController<ActorTabView>() {

    override val modules: Set<Module> = emptySet()
    override val view: ActorTabView = ActorTabView(skin, assets)

    private val world = World(events)
    private val renderer = Renderer(world, assets, events)

    private val gridWidth = 5
    private val gridLength = 5

    override fun update(deltaTime: Float) {
        drawGrid()
    }

    private fun drawGrid() {
        for (x in -5..5) {
            renderer.bgShapes.addLine(
                Vector3(x.toFloat(), -5f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
        }

        for (y in -5..5) {
            renderer.bgShapes.addLine(
                Vector3(-5f, y.toFloat(), 0f),
                Vector3(gridWidth.toFloat(), y.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
        }
    }
}
