package bke.iso.editor.actor

import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class ActorTabViewController(
    private val engine: Engine,
    private val actorTabView: ActorTabView
) {

    private val log = KotlinLogging.logger {}

    private val world = World(engine.events)
    private val actorTabRenderer = Renderer(world, engine.assets, engine.events)

    private var gridWidth = 5
    private var gridLength = 5

    private val actorModule = ActorModule(
        engine.dialogs,
        engine.serializer,
    )

    fun getModules(): Set<Module> = setOf(
        actorModule
    )

    fun enable() {
        engine.rendererManager.setActiveRenderer(actorTabRenderer)
    }

    fun update() {
        drawGrid()
    }

    private fun drawGrid() {
        for (x in -5..0) {
            actorTabRenderer.bgShapes.addLine(
                Vector3(x.toFloat(), 0f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
        }
        for (x in 0..gridWidth) {
            actorTabRenderer.bgShapes.addLine(
                Vector3(x.toFloat(), 0f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
        }
    }
}
