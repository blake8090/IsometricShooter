package bke.iso.game

import bke.iso.engine.math.Location
import bke.iso.engine.physics.collision.Bounds
import bke.iso.engine.physics.collision.Collider
import bke.iso.engine.render.DrawShadow
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.WorldService
import bke.iso.game.system.MovingPlatform
import bke.iso.service.SingletonService
import com.badlogic.gdx.math.Vector3

class EntityFactory(private val worldService: WorldService) : SingletonService {

    fun createWall(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("wall3", 0f, 16f),
                Collider(
                    Bounds(Vector3(1f, 1f, 2f), Vector3(0.5f, 0.5f, 0f)),
                    true
                )
            )

    fun createBox(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("box", 16f, 8f),
                Collider(
                    Bounds(Vector3(0.5f, 0.5f, 0.5f)),
                    true
                )
            )

    fun createTurret(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("turret", 16f, 0f),
                Turret(),
                Collider(
                    Bounds(Vector3(0.5f, 0.5f, 1f)),
                    false
                ),
                Health(3f),
                HealthBar(16f, -36f)
            )

    fun createPlayer(location: Location) =
        worldService.createEntity(location)
            .add(
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

    fun createBouncyBall(x: Float, y: Float, z: Float) =
        worldService.createEntity(x, y, z)
            .add(
                Sprite("circle", 16f, 0f),
                BouncyBall(),
                DrawShadow()
            )

    fun createPlatform(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("platform", 0f, 32f),
                MovingPlatform(),
                Collider(
                    Bounds(Vector3(2f, 1f, 0f), Vector3(1f, 0.5f, 0f)),
                    true
                )
            )

    fun createSideFence(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("fence-side", 0f, 16f),
                Collider(
                    Bounds(Vector3(0.1f, 1f, 1f), Vector3(0f, 0.5f, 0f)),
                    true
                )
            )

    fun createFrontFence(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("fence-front", 32f, 32f),
                Collider(
                    Bounds(Vector3(1f, 0.1f, 1f), Vector3(0.5f, -0.05f, 0f)),
                    true
                )
            )

    fun createLampPost(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("lamppost", 32f, 16f),
                Collider(
                    Bounds(Vector3(0.25f, 0.25f, 2.1f), Vector3(0f, 0f, 0f)),
                    true
                )
            )

    fun createPillar(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("pillar", 32f, 4f),
                Collider(
                    Bounds(Vector3(0.25f, 0.25f, 2f), Vector3(0f, 0f, 0f)),
                    true
                )
            )
}
