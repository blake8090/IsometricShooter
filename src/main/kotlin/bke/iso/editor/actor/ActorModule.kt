package bke.iso.editor.actor

import bke.iso.editor.EditorEvent
import bke.iso.editor.scene.SceneLoadedEvent
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.core.Module
import bke.iso.engine.os.Dialogs
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.scene.Scene
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import io.github.oshai.kotlinlogging.KotlinLogging

class OpenActorEvent : EditorEvent()

class ActorModule(
    private val dialogs: Dialogs,
    private val serializer: Serializer,
    private val events: Events,
    private val renderer: Renderer,
) : Module {

    private val log = KotlinLogging.logger {}

    var selectedActor: ActorPrefab? = null
        private set

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
    }

    fun draw() {
        val actor = selectedActor ?: return
        actor.components.withFirstInstance<Sprite> { sprite ->
            renderer.drawTexture(sprite.texture, Vector2(), Vector2())
        }
    }
}
