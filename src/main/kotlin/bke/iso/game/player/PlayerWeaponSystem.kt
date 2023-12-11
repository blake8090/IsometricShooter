package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.asset.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import bke.iso.game.weapon.FireType
import bke.iso.game.weapon.Inventory
import bke.iso.game.weapon.RangedWeaponItem
import bke.iso.game.weapon.RangedWeaponOffset
import bke.iso.game.weapon.RangedWeaponProperties
import bke.iso.game.weapon.WeaponProperties
import bke.iso.game.weapon.Weapons

const val SHOOT_ACTION = "shoot"
const val RELOAD_ACTION = "reload"

private const val BARREL_HEIGHT = 0.7f

class PlayerWeaponSystem(
    private val world: World,
    private val input: Input,
    private val assets: Assets,
    private val renderer: Renderer,
    private val weapons: Weapons
) : System {

    private var previousTriggerState = false

    override fun update(deltaTime: Float) {
        world.actors.each { actor: Actor, _: Player ->
            update(actor)
        }
    }

    private fun update(actor: Actor) {
        val triggerState = input.poll(SHOOT_ACTION) == 1f

        val weaponItem = actor.get<Inventory>()?.selectedWeapon
        if (weaponItem is RangedWeaponItem) {
            val properties = assets.get<WeaponProperties>(weaponItem.name) as RangedWeaponProperties
            if (canShoot(triggerState, properties)) {
                shoot(actor)
            }

            input.onAction(RELOAD_ACTION) {
                weapons.reload(weaponItem)
            }
        }

        previousTriggerState = triggerState
    }

    private fun canShoot(triggerState: Boolean, properties: RangedWeaponProperties) =
        when (properties.fireType) {
            FireType.SEMI -> {
                !previousTriggerState && triggerState // only fire when trigger was just pressed
            }

            FireType.AUTO -> {
                triggerState
            }
        }

    private fun shoot(actor: Actor) {
        val pos = actor.pos
        pos.z += BARREL_HEIGHT
        val target = toWorld(renderer.getPointerPos(), pos.z)

        actor.add(RangedWeaponOffset(0f, 0f, BARREL_HEIGHT))
        weapons.shoot(actor, target)
    }
}
