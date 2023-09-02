package bke.iso.engine.render

import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.OrderedSet
import com.badlogic.gdx.utils.Pool.Poolable
import com.badlogic.gdx.utils.Pools

class ObjectSorter {

    private val objectsBehind = mutableMapOf<SortContext, MutableSet<SortContext>>()
    private val visited = mutableSetOf<SortContext>()

    fun forEach(objects: OrderedSet<GameObject>, action: (GameObject) -> Unit) {
        objectsBehind.clear()
        visited.clear()

        val contexts = sort(objects)
        for (context in contexts) {
            callAction(context, action)
        }
    }

    private fun sort(objects: OrderedSet<GameObject>): List<SortContext> {
        val contexts = objects.map(::getSortContext)
        for (i in 0..contexts.size) {
            for (j in i + 1..<contexts.size) {
                val a = contexts.get(i)
                val b = contexts.get(j)
                if (inFront(a, b)) {
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
        return contexts
    }

    private fun callAction(context: SortContext, action: (GameObject) -> Unit) {
        if (!visited.add(context)) {
            return
        }
        for (a in objectsBehind[context].orEmpty()) {
            callAction(a, action)
        }
        action.invoke(context.obj!!)
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

        return Pools.obtain(SortContext::class.java).apply {
            this.obj = obj
            this.min.set(min)
            this.max.set(max)
            this.center.set(center)
        }
    }
}

private inline fun GameObject.getPos() =
    when (this) {
        is Tile -> location.toVector3()
        is Actor -> pos
        else -> error("Unrecognized type ${this::class.simpleName} for game object $this")
    }

data class SortContext(
    var obj: GameObject? = null,
    val min: Vector3 = Vector3(),
    val max: Vector3 = Vector3(),
    val center: Vector3 = Vector3()
) : Poolable {

    override fun reset() {
        obj = null
        min.set(0f, 0f, 0f)
        max.set(0f, 0f, 0f)
        center.set(0f, 0f, 0f)
    }
}
