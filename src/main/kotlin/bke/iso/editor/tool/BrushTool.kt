package bke.iso.editor.tool

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Location
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import mu.KotlinLogging
import kotlin.math.floor

class BrushTool(
    private val world: World,
    private val renderer: Renderer
) : EditorTool {

    private val log = KotlinLogging.logger {}

    private val referenceActor = world.actors.create(0f, 0f, 0f)
    private var selection: Selection? = null
    private var enabled = false

    override fun update() {
        // TODO: scale position when screen size changes
        val pos = toWorld(renderer.getCursorPos())

        if (selection is TileSelection) {
            pos.set(floor(pos.x), floor(pos.y), floor(pos.z))
        }
        referenceActor.moveTo(pos.x, pos.y, pos.z)

        referenceActor.get<Sprite>()?.let { sprite ->
            sprite.alpha = if (enabled) 1f else 0f
        }
    }

    override fun performAction() {
        if (selection is TileSelection) {
            createTile((selection as TileSelection).prefab)
        } else if (selection is ActorSelection) {
            createActor((selection as ActorSelection).prefab)
        }
    }

    override fun enable() {
        enabled = true
    }

    override fun disable() {
        enabled = false
    }

    fun selectPrefab(prefab: TilePrefab) {
        log.debug { "tile prefab '${prefab.name}' selected" }
        selection = TileSelection(prefab)

        referenceActor.add(Sprite(prefab.texture, 0f, 16f))
        // only need colliders when placing actors
        referenceActor.remove<Collider>()
    }

    fun selectPrefab(prefab: ActorPrefab, sprite: Sprite) {
        log.debug { "actor prefab '${prefab.name}' selected" }
        selection = ActorSelection(prefab)

        referenceActor.add(sprite.copy())
        prefab.components
            .filterIsInstance<Collider>()
            .firstOrNull()
            ?.let { referenceActor.add(it.copy()) }
    }

    private fun createTile(prefab: TilePrefab) {
        val location = Location(referenceActor.pos)
        world.setTile(location, Sprite(prefab.texture, 0f, 16f))
        log.debug { "set tile ${prefab.name} at $location" }
    }

    private fun createActor(prefab: ActorPrefab) {
        val pos = referenceActor.pos
        val sprite = referenceActor.get<Sprite>()!!
        val a = world.actors.create(
            pos.x, pos.y, pos.z,
            sprite.copy(),
        )
        prefab.components.filterIsInstance<Collider>()
            .firstOrNull()?.let { a.add(it.copy()) }
    }

    private sealed class Selection

    private class TileSelection(val prefab: TilePrefab) : Selection()

    private class ActorSelection(val prefab: ActorPrefab) : Selection()
}
