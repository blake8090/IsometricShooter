package bke.iso.editor.actor

import bke.iso.editor.scene.ui.SceneTabView
import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import io.github.oshai.kotlinlogging.KotlinLogging

class ActorTabViewController(
    private val engine: Engine,
    private val actorTabView: ActorTabView
) {

    private val log = KotlinLogging.logger { }

    private val actorModule = ActorModule(
        engine.dialogs,
        engine.serializer,
        engine.events,
        engine.renderer
    )

    fun getModules(): Set<Module> = setOf(
        actorModule
    )


}
