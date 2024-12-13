package bke.iso.editor.actor

import bke.iso.editor.withFirstInstance
import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.render.Sprite
import io.github.oshai.kotlinlogging.KotlinLogging

class ActorTabViewController(
    private val engine: Engine,
    private val actorTabView: ActorTabView
) {

    private val log = KotlinLogging.logger {}

    private val actorModule = ActorModule(
        engine.dialogs,
        engine.serializer,
    )

    fun getModules(): Set<Module> = setOf(
        actorModule
    )

    fun update() {
        val actor = actorModule.selectedActor ?: return
        actor.components.withFirstInstance<Sprite> { sprite ->
            actorTabView.setSprite(sprite)
        }
    }
}
