package bke.iso.game.actor

import bke.iso.engine.math.Location
import bke.iso.engine.physics.collision.Collider
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.debug.DebugSettings
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Description
import bke.iso.engine.world.World
import bke.iso.game.Health
import bke.iso.game.HealthBar
import com.badlogic.gdx.math.Vector3

class Factory(private val world: World) {

    fun createWall(location: Location): Actor =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("wall3", 0f, 16f),
            Collider(Vector3(1f, 1f, 2f)),
            DebugSettings().apply {
                zAxis = false
            },
            Description("wall")
        )

    fun createBox(location: Location): Actor =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("box", 16f, 8f),
            Collider(Vector3(0.5f, 0.5f, 0.5f)),
            DebugSettings(),
            Description("box")
        )

    fun createTurret(location: Location): Actor =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("turret", 16f, 0f),
            Turret(),
            Collider(
                Vector3(0.25f, 0.25f, 0.75f),
                Vector3(-0.25f, 0f, 0f)
            ),
            Health(3f),
            HealthBar(16f, -36f),
            DebugSettings(),
            Description("turret")
        )

    fun createSideFence(location: Location): Actor =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("fence-side", 0f, 16f),
            Collider(Vector3(0.1f, 1f, 1f)),
            DebugSettings().apply {
                zAxis = false
            },
            Description("side fence")
        )

    fun createFrontFence(location: Location): Actor =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("fence-front", 32f, 32f),
            Collider(Vector3(1f, 0.1f, 1f)),
            DebugSettings().apply {
                zAxis = false
            },
            Description("front fence")
        )

    fun createLampPost(location: Location): Actor =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("lamppost", 32f, 16f),
            Collider(
                Vector3(0.25f, 0.25f, 2.1f),
                Vector3(-0.125f, -0.125f, 0f)
            ),
            DebugSettings().apply {
                zAxis = false
            },
            Description("lamppost")
        )

    fun createPillar(location: Location): Actor =
        world.newActor(
            location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
            Sprite("pillar", 24f, 3f),
            Collider(Vector3(0.25f, 0.25f, 2f)),
            DebugSettings().apply {
                zAxis = false
            },
            Description("pillar")
        )
}
