package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.math.Box
import bke.iso.engine.render.Sprite

interface GameObject

class World(override val game: Game) : Module() {

    private val grid = Grid()

    val actors = Actors(grid)

    val objects: Set<GameObject>
        get() = grid.getObjects().toSet()

    override fun update(deltaTime: Float) {
        actors.update()
    }

    fun setTile(location: Location, sprite: Sprite) {
        grid.setTile(Tile(sprite, location))
    }

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
}
