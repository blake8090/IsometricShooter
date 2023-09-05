package bke.iso.game.player

import bke.iso.engine.math.Location
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.collision.Collider
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.Description
import bke.iso.engine.world.World
import bke.iso.game.combat.Health
import bke.iso.game.combat.HealthBar
import com.badlogic.gdx.math.Vector3

const val PLAYER_MAX_HEALTH: Float = 5f

class Player : Component()

fun World.createPlayer(location: Location): Actor =
    actors.create(
        location,
        Sprite("game/gfx/objects/player", 32f, 0f),
        Player(),
        Collider(
            Vector3(0.4f, 0.4f, 1.6f),
            Vector3(-0.2f, -0.2f, 0f)
        ),
        Health(PLAYER_MAX_HEALTH),
        HealthBar(18f, -64f),
        PhysicsBody(PhysicsMode.DYNAMIC),
        DebugSettings(),
        Description("player")
    )
