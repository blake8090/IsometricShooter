package bke.iso.game.entity.shadow

import bke.iso.engine.core.Event
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Module
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Entities
import bke.iso.engine.world.entity.Description
import com.badlogic.gdx.math.Vector3

const val SHADOW_SPRITE_ALPHA = 0.5f
const val SHADOW_Z_OFFSET = 0.0001f

class ShadowModule(private val world: World) : Module {

    override val alwaysActive: Boolean = false

    override fun handleEvent(event: Event) {
        if (event is Entities.CreatedEvent && event.entity.has<CastShadow>()) {
            createShadow(event.entity)
        }
    }

    private fun createShadow(parent: Entity) {
        world.entities.create(
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
