package bke.iso.engine.physics.collision

import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.math.Box
import bke.iso.engine.math.getRay
import bke.iso.engine.render.debug.DebugSettings
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
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

class Collisions(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

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

    fun checkCollisions(box: Box): Set<Collision> {
        game.renderer.debug.addBox(box, 1f, Color.SKY)
        val collisions = mutableSetOf<Collision>()
        val objects = game.world.getObjectsInArea(box)
        for (obj in objects) {
            checkCollision(box, obj)?.let(collisions::add)
        }
        return collisions
    }

    private fun checkCollision(box: Box, obj: GameObject): Collision? {
        val data = obj.getCollisionData()
        return if (data == null || !data.box.intersects(box)) {
            null
        } else {
            Collision(
                obj = obj,
                box = data.box,
                solid = data.solid,
                distance = box.dst(data.box),
                // TODO: find collision side
                side = CollisionSide.CORNER
            )
        }
    }

    fun checkCollisions(segment: Segment): Set<SegmentCollision> {
        val area = Box.from(segment)
        game.renderer.debug.addBox(area, 1f, Color.ORANGE)

        val ray = segment.getRay()
        return game.world
            .getObjectsInArea(area)
            .mapNotNull { obj -> checkCollision(segment, ray, obj) }
            .toSet()
    }

    private fun checkCollision(segment: Segment, ray: Ray, gameObject: GameObject): SegmentCollision? {
        val data = gameObject.getCollisionData()
            ?: return null

        val points = data
            .box
            .faces
            .mapNotNull { face -> findIntersection(ray, face) }
            .toSet()

        if (points.isEmpty()) {
            return null
        }

        return SegmentCollision(
            obj = gameObject,
            data = data,
            distanceStart = segment.a.dst(data.box.pos),
            distanceEnd = segment.b.dst(data.box.pos),
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
        val data = actor.getCollisionData() ?: return emptySet()
        val box = data.box

        // broad-phase: instead of iterating through every object, only check entities within general area of movement
        val px = ceil(abs(delta.x))
        val py = ceil(abs(delta.y))
        val pz = ceil(abs(delta.z))

        val projectedBox = box.expand(
            px * delta.x.sign,
            py * delta.y.sign,
            pz * delta.z.sign,
        )
        game.renderer.debug.addBox(projectedBox, 1f, Color.ORANGE)

        // narrow-phase: check precise collisions for each object within area
        val collisions = mutableSetOf<PredictedCollision>()
        for (obj in game.world.getObjectsInArea(projectedBox)) {
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

    private fun predictCollision(box: Box, delta: Vector3, gameObject: GameObject): PredictedCollision? {
        val data = gameObject.getCollisionData()
            ?: return null

        val sweptCollision = sweepTest(box, data.box, delta)
            ?: return null

        val distance = box.dst(data.box)
        val side = getCollisionSide(sweptCollision.hitNormal)
        log.trace {
            "dist: $distance, collision time: ${sweptCollision.collisionTime}," +
                    " hit normal: ${sweptCollision.hitNormal}, side: $side"
        }

        return PredictedCollision(
            obj = gameObject,
            box = data.box,
            solid = data.solid,
            distance = distance,
            collisionTime = sweptCollision.collisionTime,
            hitNormal = sweptCollision.hitNormal,
            side = side
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
            solid = predictedCollision.solid,
            distance = predictedCollision.distance,
            side = predictedCollision.side
        )
        currentCollisions
            .getOrPut(actor) { mutableSetOf() }
            .add(collision)
        actor.getOrPut(FrameCollisions())
            .collisions
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
