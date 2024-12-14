package bke.iso.editor.actor

import bke.iso.editor.EditorEvent
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import bke.iso.engine.os.Dialogs
import bke.iso.engine.render.Sprite
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class OpenActorEvent : EditorEvent()

class ActorModule(
    private val dialogs: Dialogs,
    private val serializer: Serializer,
    private val world: World
) : Module {

    private val log = KotlinLogging.logger {}

    var selectedActor: ActorPrefab? = null
        private set

    private lateinit var referenceActor: Actor

    override fun start() {
        referenceActor = world.actors.create(Vector3())
        log.debug { "started actor tab" }
    }

    override fun handleEvent(event: Event) {
        if (event is OpenActorEvent) {
            loadActor()
        }
    }

    private fun loadActor() {
        val file = dialogs.showOpenActorDialog() ?: return
        val actor = serializer.read<ActorPrefab>(file.readText())
        log.info { "Loaded actor prefab: '${file.canonicalPath}'" }
        selectedActor = actor

        actor.components.withFirstInstance<Sprite> { sprite ->
            referenceActor.add(sprite)
        }
    }
}
