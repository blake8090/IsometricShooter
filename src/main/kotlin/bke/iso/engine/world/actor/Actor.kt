package bke.iso.engine.world.actor

import bke.iso.engine.math.Location
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.world.GameObject
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.floor
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

private const val Z_CLAMP_THRESHOLD = 0.00001f

@Serializable
class Actor(
    val id: String,
    private val onMove: (Actor) -> Unit = {}
) : GameObject {

    var x: Float = 0f
        private set

    var y: Float = 0f
        private set

    var z: Float = 0f
        private set

    val pos: Vector3
        get() = Vector3(x, y, z)

    val components: MutableMap<KClass<out Component>, Component> = mutableMapOf()

    fun moveTo(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = if (abs(z) <= Z_CLAMP_THRESHOLD) {
            0f
        } else {
            z
        }
        onMove(this)
    }

    fun move(delta: Vector3) {
        move(delta.x, delta.y, delta.z)
    }

    fun move(dx: Float, dy: Float, dz: Float) {
        moveTo(x + dx, y + dy, z + dz)
    }

    fun <T : Component> add(component: T) {
        components[component::class] = component
    }

    fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    inline fun <reified T : Component> get(): T? =
        components[T::class] as T?

    inline fun <reified T : Component> getOrAdd(defaultValue: T): T =
        components.getOrPut(T::class) { defaultValue } as T

    inline fun <reified T : Component> with(action: (T) -> Unit) {
        val component = get(T::class) ?: return
        action.invoke(component)
    }

    inline fun <reified T : Component> has(): Boolean =
        components.contains(T::class)

    inline fun <reified T : Component> remove() {
        remove(T::class)
    }

    fun <T : Component> remove(componentType: KClass<T>) {
        components.remove(componentType)
    }

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
        locations.addAll(getCollisionBoxLocations())
        return locations
    }

    private fun getCollisionBoxLocations(): Set<Location> {
        val box = getCollisionBox() ?: return emptySet()

        val minX = floor(box.min.x).toInt()
        val minY = floor(box.min.y).toInt()
        val minZ = floor(box.min.z).toInt()

        val maxX = floor(box.max.x).toInt()
        val maxY = floor(box.max.y).toInt()
        val maxZ = floor(box.max.z).toInt()

        val locations = mutableSetOf<Location>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    locations.add(Location(x, y, z))
                }
            }
        }

        return locations
    }

    override fun equals(other: Any?): Boolean =
        other is Actor && other.id == id

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        val description = get<Description>()
        return if (description != null) {
            "(${description.text} id:$id)"
        } else {
            "(actor id:$id)"
        }
    }
}
