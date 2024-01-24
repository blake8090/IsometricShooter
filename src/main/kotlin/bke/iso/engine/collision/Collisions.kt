package bke.iso.engine.collision

import bke.iso.engine.math.Box
import bke.iso.engine.math.sub2
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.collision.Segment
import mu.KotlinLogging
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign

class Collisions(
    private val renderer: Renderer,
    private val world: World
) {

    private val log = KotlinLogging.logger {}

    private val previousCollisions = mutableMapOf<Actor, MutableSet<Collision>>()
    private val currentCollisions = mutableMapOf<Actor, MutableSet<Collision>>()

    fun update() {
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
            .getObjectsAt(point)
            .mapNotNullTo(mutableSetOf()) { obj -> checkCollision(point, obj) }

    private fun checkCollision(point: Vector3, obj: GameObject): PointCollision? {
        val objBox = obj.getCollisionBox()
        return if (objBox == null || !objBox.contains(point)) {
            null
        } else {
            PointCollision(obj, objBox)
        }
    }

    fun checkCollisions(box: Box): Set<Collision> {
        renderer.debug.addBox(box, 1f, Color.SKY)
        val collisions = mutableSetOf<Collision>()
        val objects = world.getObjectsInArea(box)
        for (obj in objects) {
            checkCollision(box, obj)?.let(collisions::add)
        }
        return collisions
    }

    private fun checkCollision(box: Box, obj: GameObject): Collision? {
        val objBox = obj.getCollisionBox()
        return if (objBox == null || !objBox.intersects(box)) {
            null
        } else {
            Collision(
                obj = obj,
                box = objBox,
                distance = box.dst(objBox),
                // TODO: find collision side
                side = CollisionSide.CORNER
            )
        }
    }

    fun checkCollisions(segment: Segment): Set<SegmentCollision> {
        val area = Box.fromMinMax(segment)
        renderer.debug.addBox(area, 1f, Color.ORANGE)

        val direction = Vector3(segment.b)
            .sub2(segment.a)
            .nor()
        val ray = Ray(segment.a, direction)

        return world
            .getObjectsInArea(area)
            .mapNotNull { obj -> checkCollision(segment, ray, obj) }
            .toSet()
    }

    private fun checkCollision(segment: Segment, ray: Ray, gameObject: GameObject): SegmentCollision? {
        val box = gameObject.getCollisionBox() ?: return null

        val points = box
            .getFaces()
            .mapNotNull { face -> findIntersection(ray, face) }
            .toSet()

        if (points.isEmpty()) {
            return null
        }

        return SegmentCollision(
            obj = gameObject,
            distanceStart = segment.a.dst(box.pos),
            distanceEnd = segment.b.dst(box.pos),
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
        renderer.debug.addBox(projectedBox, 1f, Color.ORANGE)

        // narrow-phase: check precise collisions for each object within area
        val collisions = mutableSetOf<PredictedCollision>()
        for (obj in world.getObjectsInArea(projectedBox)) {
            if (actor == obj) {
                continue
            }

            if (obj is Tile) {
                obj.selected = true
            } else if (obj is Actor) {
                obj.get<DebugSettings>()?.collisionBoxSelected = true
            }

            val collision = predictCollision(box, delta, obj)
            if (collision != null) {
                recordCollision(actor, collision)
                collisions.add(collision)
            }
        }

        return collisions
    }

    private fun predictCollision(box: Box, delta: Vector3, obj: GameObject): PredictedCollision? {
        val objBox = obj.getCollisionBox() ?: return null
        val sweptCollision = sweepTest(box, objBox, delta) ?: return null

        return PredictedCollision(
            obj = obj,
            box = objBox,
            distance = box.dst(objBox),
            collisionTime = sweptCollision.collisionTime,
            hitNormal = sweptCollision.hitNormal,
            side = getCollisionSide(sweptCollision.hitNormal)
        )
    }

    private fun recordCollision(actor: Actor, predictedCollision: PredictedCollision) {
        if (actor == predictedCollision.obj) {
            return
        }
        // PredictedCollision is intended only for use with Physics,
        // so the normal Collision object should be stored instead.
        val collision = Collision(
            obj = predictedCollision.obj,
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

fun GameObject.getCollisionBox(): Box? =
    when (this) {
        is Tile -> getCollisionBox()
        is Actor -> getCollisionBox()
        else -> null
    }

fun Actor.getCollisionBox(): Box? {
    val collider = get<Collider>() ?: return null
    val min = pos.add(collider.offset)
    val max = Vector3(min).add(collider.size)
    return Box.fromMinMax(min, max)
}

fun Tile.getCollisionBox(): Box =
    Box.fromMinMax(
        location.toVector3(),
        location.toVector3().add(1f, 1f, 0f)
    )
