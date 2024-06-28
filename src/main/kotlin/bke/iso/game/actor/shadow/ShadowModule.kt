package bke.iso.game.actor.shadow

import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.collision.Collider
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors
import bke.iso.engine.world.actor.Description
import com.badlogic.gdx.math.Vector3

const val SHADOW_SPRITE_ALPHA = 0.5f
const val SHADOW_Z_OFFSET = 0.0001f

class ShadowModule(private val world: World) : Module {

    override fun update(deltaTime: Float) {}

    override fun handleEvent(event: Event) {
        if (event is Actors.CreatedEvent && event.actor.has<CastShadow>()) {
            createShadow(event.actor)
        }
    }

    private fun createShadow(parent: Actor) {
        world.actors.create(
            parent.pos,
            // TODO: sprite offsets should be negative for consistency!
            Sprite("shadow.png", 16f, 16f, SHADOW_SPRITE_ALPHA),
            PhysicsBody(PhysicsMode.GHOST),
            Shadow(parent.id),
            Collider(
                Vector3(0.25f, 0.25f, SHADOW_Z_OFFSET),
                Vector3(-0.125f, -0.125f, 0f)
            ),
            Description("shadow for $parent")
        )
    }
}
