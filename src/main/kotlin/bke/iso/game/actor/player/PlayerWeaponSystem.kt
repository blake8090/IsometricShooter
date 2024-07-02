package bke.iso.game.actor.player

import bke.iso.engine.Events
import bke.iso.engine.state.System
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import bke.iso.game.weapon.FireType
import bke.iso.game.weapon.RangedWeapon
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.graphics.Color

const val SHOOT_ACTION = "shoot"
const val RELOAD_ACTION = "reload"

private const val BARREL_HEIGHT = 0.7f

class PlayerWeaponSystem(
    private val world: World,
    private val input: Input,
    private val renderer: Renderer,
    private val events: Events,
    private val weaponsModule: WeaponsModule
) : System {

    private var previousTriggerState = false

    override fun update(deltaTime: Float) {
        world.actors.each { actor: Actor, _: Player ->
            update(actor)
        }
    }

    private fun update(actor: Actor) {
        val triggerState = input.poll(SHOOT_ACTION) == 1f

        if (canShoot(actor, triggerState)) {
            shoot(actor)
        }

        input.onAction(RELOAD_ACTION) {
            events.fire(WeaponsModule.ReloadEvent(actor))
        }

        previousTriggerState = triggerState

        renderer.fgShapes.addLine(
            weaponsModule.getShootPos(actor),
            renderer.pointer.worldPos,
            1.25f,
            Color.RED
        )
    }

    private fun canShoot(actor: Actor, triggerState: Boolean): Boolean {
        val weapon = weaponsModule.getSelectedWeapon(actor)
        if (weapon !is RangedWeapon) {
            return false
        }

        val properties = weaponsModule.getProperties(weapon)
        return when (properties.fireType) {
            FireType.SEMI -> {
                !previousTriggerState && triggerState // only fire when trigger was just pressed
            }

            FireType.AUTO -> {
                triggerState
            }
        }
    }

    private fun shoot(actor: Actor) {
        val pos = actor.pos
        pos.z += BARREL_HEIGHT
        val target = toWorld(renderer.pointer.pos, pos.z)

        events.fire(WeaponsModule.ShootEvent(actor, target))

        val weapon = weaponsModule.getSelectedWeapon(actor)
        if (weapon is RangedWeapon && weapon.ammo <= 0f) {
            events.fire(WeaponsModule.ReloadEvent(actor))
        }
    }
}
