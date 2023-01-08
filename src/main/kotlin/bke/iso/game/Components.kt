package bke.iso.game

import bke.iso.engine.entity.Component
import com.badlogic.gdx.math.Vector2
import java.util.UUID

class Player : Component()

data class Bullet(
    val shooterId: UUID,
    val startPos: Vector2
) : Component()

class Turret() : Component() {
    var coolDownTime: Float = 0f
}

data class Health(
    val maxValue: Float,
    var value: Float = maxValue
) : Component()

data class HealthBar(
    val offsetX: Float,
    val offsetY: Float
) : Component()