package bke.iso.engine.collision

import bke.iso.engine.core.EngineModule
import bke.iso.engine.math.Box
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.entity.Actor
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.collision.Segment
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

    private val previousCollisions = mutableMapOf<Actor, MutableSet<Collision>>()
    private val currentCollisions = mutableMapOf<Actor, MutableSet<Collision>>()

    override fun update(deltaTime: Float) {
        previousCollisions.clear()
        previousCollisions.putAll(currentCollisions)
        currentCollisions.clear()
    }

    fun getCollisions(actor: Actor): Set<Collision> =
        currentCollisions[actor].orEmpty()

    fun getPreviousCollisions(actor: Actor): Set<Collision> =
        previousCollisions[actor].orEmpty()

    fun checkCollisions(point: Vector3): Set<PointCollision> =
        world
            .actors
            .findAllAt(point)
            .mapNotNullTo(mutableSetOf()) { actor -> checkCollision(point, actor) }

    private fun checkCollision(point: Vector3, actor: Actor): PointCollision? {
        val box = actor.getCollisionBox()
        return if (box == null || !box.contains(point)) {
            null
        } else {
            PointCollision(actor, box)
        }
    }

    fun checkCollisions(box: Box): Set<Collision> {
        renderer.debug.category("collisions").addBox(box, 1f, Color.SKY)
        val collisions = mutableSetOf<Collision>()
        for (actor in world.actors.findAllIn(box)) {
            checkCollision(box, actor)?.let(collisions::add)
        }
        return collisions
    }

    private fun checkCollision(box: Box, actor: Actor): Collision? {
        val actorBox = actor.getCollisionBox()
        return if (actorBox == null || !actorBox.intersects(box)) {
            null
        } else {
            Collision(
                actor = actor,
                box = actorBox,
                distance = box.dst(actorBox),
                // TODO: find collision side
                side = CollisionSide.CORNER
            )
        }
    }

    fun checkLineCollisions(start: Vector3, end: Vector3): Set<SegmentCollision> {
        val area = Box.fromMinMax(Segment(start, end))
        renderer.debug.category("collisions").addBox(area, 1f, Color.ORANGE)

        val direction = Vector3(end)
            .sub(start)
            .nor()
        val ray = Ray(start, direction)

        return world
            .actors
            .findAllIn(area)
            .mapNotNull { actor -> checkLineCollision(start, end, ray, actor) }
            .toSet()
    }

    private fun checkLineCollision(start: Vector3, end: Vector3, ray: Ray, actor: Actor): SegmentCollision? {
        val box = actor.getCollisionBox() ?: return null

        val points = box
            .getFaces()
            .mapNotNull { face -> findIntersection(ray, face) }
            .toSet()

        if (points.isEmpty()) {
            return null
        }

        return SegmentCollision(
            actor = actor,
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

    fun predictCollisions(actor: Actor, delta: Vector3): Set<PredictedCollision> {
        val box = actor.getCollisionBox() ?: return emptySet()

        // broad-phase: instead of iterating through every object, only check entities within general area of movement
        val px = ceil(abs(delta.x))
        val py = ceil(abs(delta.y))
        val pz = ceil(abs(delta.z))

        val projectedBox = box.expand(
            px * delta.x.sign,
            py * delta.y.sign,
            pz * delta.z.sign,
        )
        renderer.debug.category("collisions").addBox(projectedBox, 1f, Color.ORANGE)

        // narrow-phase: check precise collisions for each object within area
        val collisions = mutableSetOf<PredictedCollision>()
        for (otherActor in world.actors.findAllIn(projectedBox)) {
            if (actor == otherActor) {
                continue
            }

            otherActor.get<DebugSettings>()?.collisionBoxSelected = true

            val collision = predictCollision(box, delta, otherActor)
            if (collision != null) {
                recordCollision(actor, collision)
                collisions.add(collision)
            }
        }

        return collisions
    }

    private fun predictCollision(box: Box, delta: Vector3, actor: Actor): PredictedCollision? {
        val actorBox = actor.getCollisionBox() ?: return null
        val sweptCollision = sweepTest(box, actorBox, delta) ?: return null

        return PredictedCollision(
            actor = actor,
            box = actorBox,
            distance = box.dst(actorBox),
            collisionTime = sweptCollision.collisionTime,
            hitNormal = sweptCollision.hitNormal,
            side = getCollisionSide(sweptCollision.hitNormal)
        )
    }

    private fun recordCollision(actor: Actor, predictedCollision: PredictedCollision) {
        if (actor == predictedCollision.actor) {
            return
        }
        // PredictedCollision is intended only for use with Physics,
        // so the normal Collision object should be stored instead.
        val collision = Collision(
            actor = predictedCollision.actor,
            box = predictedCollision.box,
            distance = predictedCollision.distance,
            side = predictedCollision.side
        )
        currentCollisions
            .getOrPut(actor) { mutableSetOf() }
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

// TODO: clean this up!
fun Actor.getCollisionBox(): Box? {
    return if (has<Tile>()) {
        Box.fromMinMax(
            pos,
            pos.add(1f, 1f, 0f)
        )
    } else {
        val collider = get<Collider>() ?: return null
        val min = pos.add(collider.offset)
        val max = Vector3(min).add(collider.size)
        Box.fromMinMax(min, max)
    }
}
