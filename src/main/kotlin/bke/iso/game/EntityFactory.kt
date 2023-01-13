package bke.iso.game

import bke.iso.service.Singleton
import bke.iso.engine.entity.EntityService
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collision
import bke.iso.engine.render.Sprite

@Singleton
class EntityFactory(private val entityService: EntityService) {

    fun createWall(x: Float, y: Float) =
        entityService.create(x, y)
            .add(
                Sprite("wall3", 0f, 16f),
                Collision(
                    Bounds(1f, 1f, 0f, 0f),
                    true
                )
            )

    fun createBox(x: Float, y: Float) =
        entityService.create(x, y)
            .add(
                Sprite("box", 16f, 8f),
                Collision(
                    Bounds(0.5f, 0.5f, -0.25f, -0.25f),
                    true
                )
            )

    fun createTurret(x: Float, y: Float) =
        entityService.create(x, y)
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

    fun createPlayer(x: Float, y: Float) =
        entityService.create(x, y)
            .add(
                Sprite("player", 32f, 0f),
                Player(),
                Collision(
                    Bounds(0.5f, 0.5f, -0.25f, -0.25f),
                    false
                ),
                Health(5f),
                HealthBar(18f, -64f)
            )
}
