package bke.iso.v2.game

import bke.iso.engine.math.Location
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collider
import bke.iso.engine.render.DrawShadow
import bke.iso.engine.render.Sprite
import bke.iso.game.combat.Health
import bke.iso.game.combat.HealthBar
import bke.iso.game.entity.MovingPlatform
import bke.iso.game.entity.Player
import bke.iso.game.entity.Turret
import bke.iso.v2.engine.world.World
import com.badlogic.gdx.math.Vector3

class Factory(private val world: World) {

    fun createPlayer(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("player", 32f, 0f),
            Player(),
            Collider(
                Bounds(Vector3(0.5f, 0.5f, 1.6f)),
                false
            ),
            Health(5f),
            HealthBar(18f, -64f),
            DrawShadow()
        )

    fun createWall(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("wall3", 0f, 16f),
            Collider(
                Bounds(Vector3(1f, 1f, 2f), Vector3(0.5f, 0.5f, 0f)),
                true
            )
        )

    fun createBox(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("box", 16f, 8f),
            Collider(
                Bounds(Vector3(0.5f, 0.5f, 0.5f)),
                true
            )
        )

    fun createTurret(location: Location)  =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("turret", 16f, 0f),
            Turret(),
            Collider(
                Bounds(Vector3(0.5f, 0.5f, 1f)),
                false
            ),
            Health(3f),
            HealthBar(16f, -36f)
        )

    fun createPlatform(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("platform", 0f, 32f),
            MovingPlatform(),
            Collider(
                Bounds(Vector3(2f, 1f, 0f), Vector3(1f, 0.5f, 0f)),
                true
            )
        )

    fun createSideFence(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("fence-side", 0f, 16f),
            Collider(
                Bounds(Vector3(0.1f, 1f, 1f), Vector3(0f, 0.5f, 0f)),
                true
            )
        )

    fun createFrontFence(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("fence-front", 32f, 32f),
            Collider(
                Bounds(Vector3(1f, 0.1f, 1f), Vector3(0.5f, -0.05f, 0f)),
                true
            )
        )

    fun createLampPost(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("lamppost", 32f, 16f),
            Collider(
                Bounds(Vector3(0.25f, 0.25f, 2.1f), Vector3(0f, 0f, 0f)),
                true
            )
        )

    fun createPillar(location: Location) =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("pillar", 32f, 4f),
            Collider(
                Bounds(Vector3(0.25f, 0.25f, 2f), Vector3(0f, 0f, 0f)),
                true
            )
        )
}
