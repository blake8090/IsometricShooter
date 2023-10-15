package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.math.Box
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.math.Vector3

interface GameObject

class World {

    private val grid = Grid()

    val actors = Actors(grid)

    // TODO: property?
    fun getObjects() = grid.getObjects()

    fun update() {
        actors.update()
    }

    fun setTile(location: Location, sprite: Sprite) {
        grid.setTile(Tile(sprite, location))
    }

    fun getObjectsAt(point: Vector3): Set<GameObject> =
        grid.objectsAt(Location(point))

    fun getObjectsInArea(box: Box): Set<GameObject> {
        val minX = box.min.x.toInt()
        val minY = box.min.y.toInt()
        val minZ = box.min.z.toInt()

        val maxX = box.max.x.toInt()
        val maxY = box.max.y.toInt()
        val maxZ = box.max.z.toInt()

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

    fun clear() {
        grid.clear()
    }
}
