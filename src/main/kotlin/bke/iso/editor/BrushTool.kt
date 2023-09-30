package bke.iso.editor

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import mu.KotlinLogging
import kotlin.math.floor

class BrushTool(
    world: World,
    private val renderer: Renderer
) {

    private val log = KotlinLogging.logger {}

    private val referenceActor = world.actors.create(0f, 0f, 0f)
    private var selection: Selection? = null

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

    fun update() {
        // TODO: scale position when screen size changes
        val pos = toWorld(renderer.getCursorPos())

        if (selection is TileSelection) {
            pos.set(floor(pos.x), floor(pos.y), floor(pos.z))
        }
        referenceActor.moveTo(pos.x, pos.y, pos.z)
    }

//    private fun createTile(prefab: TilePrefab, location: Location) {
//        log.debug { "set tile ${prefab.name} at $location" }
//        game.world.setTile(location, Sprite(prefab.texture, 0f, 16f))
//    }
//
//    private fun createActor(prefab: ActorPrefab, pos: Vector3) {
//        val sprite = referenceActor.get<Sprite>()!!
//        val a = game.world.actors.create(
//            pos.x, pos.y, pos.z,
//            sprite.copy(),
//            ReferencePrefab(prefab)
//        )
//        prefab.components.filterIsInstance<Collider>()
//            .firstOrNull()?.let { a.add(it.copy()) }
//    }

    private sealed class Selection

    private class TileSelection(val prefab: TilePrefab) : Selection()

    private class ActorSelection(val prefab: ActorPrefab) : Selection()
}
