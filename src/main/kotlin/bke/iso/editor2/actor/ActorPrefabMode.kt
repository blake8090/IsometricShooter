package bke.iso.editor2.actor

import bke.iso.editor2.EditorMode
import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World

class ActorPrefabMode(
    events: Events,
    assets: Assets,
) : EditorMode() {

    override val world = World(events)
    override val renderer = Renderer(world, assets, events)

    private val view = ActorPrefabModeView(assets, events)

    override fun start() {
    }

    override fun stop() {
    }

    override fun update() {
    }

    override fun draw() {
        view.draw()
    }

    override fun handleEvent(event: Event) {
    }
}
