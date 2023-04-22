package bke.iso.game

import bke.iso.engine.math.Location
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collision
import bke.iso.engine.physics.collision.BoundsV2
import bke.iso.engine.physics.collision.CollisionV2
import bke.iso.engine.render.DrawShadow
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.WorldService
import bke.iso.service.SingletonService
import com.badlogic.gdx.math.Vector3

class EntityFactory(private val worldService: WorldService): SingletonService {

    fun createWall(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("wall3", 0f, 16f),
                Collision(
                    Bounds(1f, 1f, 2f, 0f, 0f),
                    true
                ),
                CollisionV2(
                    BoundsV2(Vector3(1f, 1f, 2f), Vector3(0.5f, 0.5f, 0f)),
                    true
                )
            )

    fun createBox(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("box", 16f, 8f),
                Collision(
                    Bounds(0.5f, 0.5f, 0.5f, -0.25f, -0.25f),
                    true
                ),
                CollisionV2(
                    BoundsV2(Vector3(0.5f, 0.5f, 0.5f)),
                    true
                )
            )

    fun createTurret(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("turret", 16f, 0f),
                Turret(),
                Collision(
                    Bounds(0.5f, 0.5f, 1f, -0.25f, -0.25f),
                    false
                ),
                CollisionV2(
                    BoundsV2(Vector3(0.5f, 0.5f, 1f)),
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
                Collision(
                    Bounds(0.5f, 0.5f, 2f, -0.25f, -0.25f),
                    false
                ),
                CollisionV2(
                    BoundsV2(Vector3(0.5f, 0.5f, 2f)),
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
}
