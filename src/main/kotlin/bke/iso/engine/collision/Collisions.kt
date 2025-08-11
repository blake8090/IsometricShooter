package bke.iso.engine.collision

import bke.iso.engine.core.EngineModule
import bke.iso.engine.math.Box
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.collision.Segment
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.Pools
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign

class Collisions(
    private val renderer: Renderer,
    private val world: World
) : EngineModule() {

    override val moduleName = "collisions"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    private val previousCollisions = mutableMapOf<Entity, MutableSet<Collision>>()
    private val currentCollisions = mutableMapOf<Entity, MutableSet<Collision>>()

    override fun update(deltaTime: Float) {
        previousCollisions.clear()
        previousCollisions.putAll(currentCollisions)
        currentCollisions.clear()
    }

    fun getCollisions(entity: Entity): Set<Collision> =
        currentCollisions[entity].orEmpty()

    fun getPreviousCollisions(entity: Entity): Set<Collision> =
        previousCollisions[entity].orEmpty()

    fun checkCollisions(point: Vector3): Set<PointCollision> =
        world
            .entities
            .findAllAt(point)
            .mapNotNullTo(mutableSetOf()) { entity -> checkCollision(point, entity) }

    private fun checkCollision(point: Vector3, entity: Entity): PointCollision? {
        val box = entity.getCollisionBox()
        return if (box == null || !box.contains(point)) {
            null
        } else {
            PointCollision(entity, box)
        }
    }

    fun checkCollisions(box: Box): Set<Collision> {
        renderer.debug.category("collisions").addBox(box, 1f, Color.SKY)
        val collisions = mutableSetOf<Collision>()
        for (entity in world.entities.findAllIn(box)) {
            checkCollision(box, entity)?.let(collisions::add)
        }
        return collisions
    }

    private fun checkCollision(box: Box, other: Entity): Collision? {
        val otherBox = other.getCollisionBox()
        return if (otherBox == null || !otherBox.intersects(box)) {
            null
        } else {
            Collision(
                entity = other,
                box = otherBox,
                distance = box.dst(otherBox),
                // TODO: find collision side
                side = CollisionSide.CORNER
            )
        }
    }

    fun checkLineCollisions(start: Vector3, end: Vector3): Array<SegmentCollision> {
        val area = Box.fromMinMax(Segment(start, end))
        renderer.debug.category("collisions").addBox(area, 1f, Color.ORANGE)

        val direction = Vector3(end)
            .sub(start)
            .nor()
        val ray = Ray(start, direction)

        val collisions = Array<SegmentCollision>()
        for (entity in world.entities.findAllIn(area)) {
            val collision = checkLineCollision(start, end, ray, entity)
            if (collision != null) {
                collisions.add(collision)
            }
        }

        return collisions
    }

    private fun checkLineCollision(start: Vector3, end: Vector3, ray: Ray, entity: Entity): SegmentCollision? {
        val box = entity.getCollisionBox() ?: return null

        val points = ObjectSet<Vector3>()
        for (face in box.getFaces()) {
            val point = findIntersection(ray, face)
            if (point != null) {
                points.add(point)
            }
            Pools.free(face)
        }

        if (points.isEmpty) {
            return null
        }

        return SegmentCollision(
            entity = entity,
            distanceStart = start.dst(box.pos),
            distanceEnd = end.dst(box.pos),
            points = points
        )
    }

    private fun findIntersection(ray: Ray, box: BoundingBox): Vector3? {
        val point = Vector3()
        return if (Intersector.intersectRayBounds(ray, box, point)) {
            point
        } else {
            null
        }
    }

    fun predictCollisions(entity: Entity, delta: Vector3): Set<PredictedCollision> {
        val box = entity.getCollisionBox() ?: return emptySet()

        // broad-phase: instead of iterating through every object, only check entities within general area of movement
        val px = ceil(abs(delta.x))
        val py = ceil(abs(delta.y))
        val pz = ceil(abs(delta.z))

        val projectedBox = box.expand(
            px * delta.x.sign,
            py * delta.y.sign,
            pz * delta.z.sign,
        )
        renderer.debug.category("collisions").addBox(projectedBox, 0.5f, Color.ORANGE)
        renderer.debug.category("collisions").addBox(box, 1f, Color.ORANGE)

        // narrow-phase: check precise collisions for each object within area
        val collisions = mutableSetOf<PredictedCollision>()
        for (otherEntity in world.entities.findAllIn(projectedBox)) {
            if (entity == otherEntity) {
                continue
            }

            otherEntity.get<DebugSettings>()?.collisionBoxSelected = true

            val collision = predictCollision(box, delta, otherEntity)
            if (collision != null) {
                recordCollision(entity, collision)
                collisions.add(collision)
            }
        }

        return collisions
    }

    private fun predictCollision(box: Box, delta: Vector3, entity: Entity): PredictedCollision? {
        val collisionBox = entity.getCollisionBox() ?: return null
        val sweptCollision = sweepTest(box, collisionBox, delta) ?: return null

        return PredictedCollision(
            entity = entity,
            box = collisionBox,
            distance = box.dst(collisionBox),
            collisionTime = sweptCollision.collisionTime,
            hitNormal = sweptCollision.hitNormal,
            side = getCollisionSide(sweptCollision.hitNormal)
        )
    }

    private fun recordCollision(entity: Entity, predictedCollision: PredictedCollision) {
        if (entity == predictedCollision.entity) {
            return
        }
        // PredictedCollision is intended only for use with Physics,
        // so the normal Collision object should be stored instead.
        val collision = Collision(
            entity = predictedCollision.entity,
            box = predictedCollision.box,
            distance = predictedCollision.distance,
            side = predictedCollision.side
        )
        currentCollisions
            .getOrPut(entity) { mutableSetOf() }
            .add(collision)
    }

    private fun getCollisionSide(hitNormal: Vector3): CollisionSide =
        when (hitNormal) {
            Vector3(-1f, 0f, 0f) -> CollisionSide.LEFT
            Vector3(1f, 0f, 0f) -> CollisionSide.RIGHT
            Vector3(0f, -1f, 0f) -> CollisionSide.FRONT
            Vector3(0f, 1f, 0f) -> CollisionSide.BACK
            Vector3(0f, 0f, -1f) -> CollisionSide.BOTTOM
            Vector3(0f, 0f, 1f) -> CollisionSide.TOP
            else -> CollisionSide.CORNER
        }

}

fun Entity.getCollisionBox(): Box? {
    val collider = get<Collider>() ?: return null
    val min = pos.add(collider.offset)
    val max = Vector3(min).add(collider.size)
    return Box.fromMinMax(min, max)
}
