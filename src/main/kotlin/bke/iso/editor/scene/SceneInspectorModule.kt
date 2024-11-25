package bke.iso.editor.scene

import bke.iso.editor.scene.tool.PointerDeselectActorEvent
import bke.iso.editor.scene.tool.PointerSelectActorEvent
import bke.iso.editor.scene.ui.SceneInspectorView
import bke.iso.engine.Event
import bke.iso.engine.state.Module
import bke.iso.engine.world.actor.Description
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneInspectorModule(
    private val sceneInspectorView: SceneInspectorView
) : Module {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {}

    override fun handleEvent(event: Event) {
        if (event is PointerSelectActorEvent) {
            log.debug { "selected ${event.actor}" }

            val description = event.actor
                .get<Description>()
                ?.text
                ?: ""

            sceneInspectorView.updateProperties(
                id = event.actor.id,
                description = description,
                pos = event.actor.pos
            )
        } else if (event is PointerDeselectActorEvent) {
            log.debug { "nothing selected - clearing inspector" }
        }
    }
}
