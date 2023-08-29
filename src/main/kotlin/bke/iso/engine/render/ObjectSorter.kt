package bke.iso.engine.render

import bke.iso.engine.physics.collision.getCollisionBox
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import com.badlogic.gdx.math.Vector3

class ObjectSorter {

    private val objectsBehind = mutableMapOf<SortContext, MutableSet<SortContext>>()
    private val visited = mutableSetOf<SortContext>()

    fun forEach(objects: Collection<GameObject>, action: (GameObject) -> Unit) {
        objectsBehind.clear()
        visited.clear()

        val contexts = objects.map(::getSortContext)
        for ((i, a) in contexts.withIndex()) {
            for ((j, b) in contexts.withIndex()) {
                if (i == j) {
                    continue
                } else if (inFront(a, b)) {
                    objectsBehind
                        .getOrPut(a) { mutableSetOf() }
                        .add(b)
                } else if (inFront(b, a)) {
                    objectsBehind
                        .getOrPut(b) { mutableSetOf() }
                        .add(a)
                }
            }
        }

        for (context in contexts) {
            callAction(context, action)
        }
    }

    private fun callAction(context: SortContext, action: (GameObject) -> Unit) {
        if (!visited.add(context)) {
            return
        }
        for (a in objectsBehind[context].orEmpty()) {
            callAction(a, action)
        }
        action.invoke(context.obj)
    }

    private fun inFront(a: SortContext, b: SortContext): Boolean {
        if (a.max.z <= b.min.z) {
            return false
        }

        if (a.min.y - b.max.y >= 0) {
            return false
        }

        if (a.max.x - b.min.x <= 0) {
            return false
        }

        return true
    }

    private fun getSortContext(obj: GameObject): SortContext {
        // TODO: should this use a different class to separate rendering and collision?
        val box = obj.getCollisionBox()
        val pos = obj.getPos()

        val min = box?.min ?: pos
        val max = box?.max ?: pos
        val center = box?.pos ?: pos
        return SortContext(obj, min, max, center)
    }
}

private fun GameObject.getPos() =
    when (this) {
        is Tile -> location.toVector3()
        is Actor -> pos
        else -> error("Unrecognized type ${this::class.simpleName} for game object $this")
    }

private data class SortContext(
    val obj: GameObject,
    val min: Vector3,
    val max: Vector3,
    val center: Vector3
)
