package bke.iso.editor

import bke.iso.editor.event.EditorEvent
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.os.Dialogs
import bke.iso.engine.scene.ActorRecord
import bke.iso.engine.scene.Scene
import bke.iso.engine.scene.TileRecord
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import kotlinx.serialization.encodeToString
import mu.KotlinLogging

class SaveSceneEvent : EditorEvent()

class OpenSceneEvent : EditorEvent()

class SceneModule(
    private val dialogs: Dialogs,
    private val serializer: Serializer,
    private val world: World,
    private val assets: Assets,
    private val referenceActors: ReferenceActors
) : Module {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        val ctrlPressed =
            Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
        if (ctrlPressed && Gdx.input.isKeyPressed(Input.Keys.O)) {
            loadScene()
        } else if (ctrlPressed && Gdx.input.isKeyPressed(Input.Keys.S)) {
            saveScene()
        }
    }

    override fun handleEvent(event: Event) {
        if (event is SaveSceneEvent) {
            saveScene()
        } else if (event is OpenSceneEvent) {
            loadScene()
        }
    }

    private fun loadScene() {
        val file = dialogs.showOpenFileDialog() ?: return
        val scene = serializer.read<Scene>(file.readText())

        referenceActors.clear()
        world.clear()

        for (record in scene.actors) {
            val prefab = assets.get<ActorPrefab>(record.prefab)
            referenceActors.create(prefab, record.pos)
        }

        for (record in scene.tiles) {
            val prefab = assets.get<TilePrefab>(record.prefab)
            referenceActors.create(prefab, record.location)
        }

        log.info { "Loaded scene: '${file.canonicalPath}'" }
    }

    private fun saveScene() {
        val file = dialogs.showSaveFileDialog() ?: return

        val actors = mutableListOf<ActorRecord>()
        world.actors.each { actor: Actor, reference: ActorPrefabReference ->
            actors.add(ActorRecord(actor.pos, reference.prefab))
        }

        val tiles = mutableListOf<TileRecord>()
        world.actors.each { actor: Actor, reference: TilePrefabReference ->
            tiles.add(TileRecord(Location(actor.pos), reference.prefab))
        }

        val scene = Scene("1", actors, tiles)
        val content = serializer.format.encodeToString(scene)
        file.writeText(content)
        log.info { "Saved scene: '${file.canonicalPath}'" }
    }
}
