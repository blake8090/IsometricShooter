package bke.iso.engine.world

import bke.iso.engine.Game
import bke.iso.engine.math.Location
import bke.iso.engine.math.Box
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors
import com.badlogic.gdx.math.Vector3
import kotlin.math.ceil
import kotlin.math.floor

interface GameObject

class World(events: Game.Events) {

    private val grid = Grid()

    val actors = Actors(grid, events)
    val buildings = Buildings()

    private val deletedActors = mutableSetOf<Actor>()

    // TODO: property?
    fun getObjects() = grid.getObjects()

    fun update() {
        for (actor in deletedActors) {
            grid.remove(actor)
            buildings.remove(actor)
        }
        deletedActors.clear()
    }

    fun setTile(location: Location, sprite: Sprite): Tile {
        val tile = Tile(sprite, location)
        grid.setTile(tile)
        return tile
    }

    fun getObjectsAt(point: Vector3): Set<GameObject> =
        grid.objectsAt(Location(point))

    fun getObjectsInArea(box: Box): Set<GameObject> {
        val minX = floor(box.min.x).toInt()
        val minY = floor(box.min.y).toInt()
        val minZ = floor(box.min.z).toInt()

        val maxX = ceil(box.max.x).toInt()
        val maxY = ceil(box.max.y).toInt()
        val maxZ = ceil(box.max.z).toInt()

        val objects = mutableSetOf<GameObject>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    objects.addAll(grid.objectsAt(Location(x, y, z)))
                }
            }
        }
        return objects
    }

    fun delete(actor: Actor) {
        deletedActors.add(actor)
    }

    fun clear() {
        grid.clear()
        buildings.clear()
    }
}
