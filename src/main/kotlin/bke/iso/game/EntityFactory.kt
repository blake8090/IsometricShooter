package bke.iso.game

import bke.iso.service.Singleton
import bke.iso.engine.math.Location
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collision
import bke.iso.engine.render.DrawShadow
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.WorldService

@Singleton
class EntityFactory(private val worldService: WorldService) {

    fun createWall(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("wall3", 0f, 16f),
                Collision(
                    Bounds(1f, 1f, 0f, 0f),
                    true
                )
            )

    fun createBox(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("box", 16f, 8f),
                Collision(
                    Bounds(0.5f, 0.5f, -0.25f, -0.25f),
                    true
                )
            )

    fun createTurret(location: Location) =
        worldService.createEntity(location)
            .add(
                Sprite("turret", 16f, 0f),
                Turret(),
                Collision(
                    Bounds(0.5f, 0.5f, -0.25f, -0.25f),
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
                    Bounds(0.5f, 0.5f, -0.25f, -0.25f),
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
