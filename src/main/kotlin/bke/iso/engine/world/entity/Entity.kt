package bke.iso.engine.world.entity

import bke.iso.engine.math.Location
import bke.iso.engine.collision.getCollisionBox
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.reflect.KClass

private const val Z_CLAMP_THRESHOLD = 0.00001f

@Serializable
class Entity(
    val id: String,
    private val onMove: (Entity) -> Unit = {},
    private val onComponentAdded: (Entity, Component) -> Unit = { _, _ -> },
    private val onComponentRemoved: (Entity, Component) -> Unit = { _, _ -> },
) {

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
        if (this.x == x && this.y == y && this.z == z) {
            return
        }

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
        onComponentAdded.invoke(this, component)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> get(type: KClass<T>): T? {
        val component = components[type]
        return if (component == null) {
            null
        } else {
            component as T
        }
    }

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
        val component = components.remove(componentType)
        if (component != null) {
            onComponentRemoved.invoke(this, component)
        }
    }

    /**
     * Returns a list of locations that the entity spans, including its bounding box (if present).
     *
     * For example: An entity is positioned at (0, 0, 0) with a bounding box of size 1.
     * The box's minimum is therefore (0, 0, 0) and the maximum is (1, 1, 1).
     * Since the box only overlaps the (0, 0, 0) location, only that location is returned.
     *
     * For a box with min (0.5, 0.5, 0) and max (1.5, 1.5, 1), the returned locations would be:
     * (0, 0, 0), (0, 1, 0), (1, 0, 0), (1, 1, 0)
     */
    fun getLocations(): Set<Location> {
        val locations = mutableSetOf<Location>()
        locations.add(Location(pos))
        locations.addAll(getCollisionBoxLocations())
        return locations
    }

    private fun getCollisionBoxLocations(): Set<Location> {
        val box = getCollisionBox() ?: return emptySet()

        // Use floor for min and ceil for max to properly handle fractional positions
        // This ensures we only include locations that the box actually overlaps
        val minX = floor(box.min.x).toInt()
        val minY = floor(box.min.y).toInt()
        val minZ = floor(box.min.z).toInt()

        val maxX = ceil(box.max.x).toInt()
        val maxY = ceil(box.max.y).toInt()
        val maxZ = ceil(box.max.z).toInt()

        val locations = mutableSetOf<Location>()
        for (x in minX until maxX) {
            for (y in minY until maxY) {
                for (z in minZ until maxZ) {
                    locations.add(Location(x, y, z))
                }
            }
        }

        return locations
    }

    override fun equals(other: Any?): Boolean =
        other is Entity && other.id == id

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        val description = get<Description>()
        return if (description != null) {
            "(${description.text} id:$id)"
        } else {
            "(entity id:$id)"
        }
    }
}
