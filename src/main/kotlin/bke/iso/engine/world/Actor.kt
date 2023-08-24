package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.physics.getCollisionData
import com.badlogic.gdx.math.Vector3
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

open class Component

class Actor(
    val id: UUID = UUID.randomUUID(),
    private val onMove: (Actor) -> Unit = {}
) : GameObject() {

    var x: Float = 0f
        private set

    var y: Float = 0f
        private set

    var z: Float = 0f
        private set

    val pos: Vector3
        get() = Vector3(x, y, z)

    val components = mutableMapOf<KClass<out Component>, Component>()

    fun moveTo(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
        onMove(this)
    }

    fun move(delta: Vector3) =
        move(delta.x, delta.y, delta.z)

    fun move(dx: Float, dy: Float, dz: Float) =
        moveTo(x + dx, y + dy, z + dz)

    inline fun <reified T : Component> add(component: T) {
        components[T::class] = component
    }

    fun add(vararg components: Component) {
        for (component in components) {
            add(component)
        }
    }

    fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    inline fun <reified T : Component> get(): T? =
        components[T::class] as T?

    inline fun <reified T : Component> getOrPut(defaultValue: T): T =
        components.getOrPut(T::class) { defaultValue } as T

    inline fun <reified T : Component> has() =
        components.contains(T::class)

    inline fun <reified T : Component> remove() =
        components.remove(T::class)

    /**
     * Returns a list of locations that the actor spans, including its bounding box (if present).
     *
     * For example: An actor is positioned at (0, 0, 0) with a bounding box of size 1.
     * The box's minimum is therefore (0, 0, 0) and the maximum is (1, 1, 1).
     *
     * The returned locations would be the following:
     *
     * (0, 0, 0) (0, 1, 0) (1, 0, 0) (1, 1, 0)
     * (0, 0, 1) (0, 1, 1) (1, 0, 1) (1, 1, 1)
     */
    fun getLocations(): Set<Location> {
        val locations = mutableSetOf<Location>()
        locations.add(Location(pos))

        val collisionData = getCollisionData()
        if (collisionData != null) {
            val minX = collisionData.box.min.x.toInt()
            val minY = collisionData.box.min.y.toInt()
            val minZ = collisionData.box.min.z.toInt()

            val maxX = collisionData.box.max.x.toInt()
            val maxY = collisionData.box.max.y.toInt()
            val maxZ = collisionData.box.max.z.toInt()

            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    for (z in minZ..maxZ) {
                        locations.add(Location(x, y, z))
                    }
                }
            }
        }

        return locations
    }

    override fun equals(other: Any?) =
        other is Actor && other.id == id

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString() =
        id.toString()
}
