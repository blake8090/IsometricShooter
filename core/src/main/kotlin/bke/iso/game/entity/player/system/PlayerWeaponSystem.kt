package bke.iso.game.entity.player.system

import bke.iso.engine.core.Events
import bke.iso.engine.state.System
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.World
import bke.iso.game.entity.player.Player
import bke.iso.game.weapon.FireType
import bke.iso.game.weapon.system.RangedWeapon
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.applyRangedWeaponOffset
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

const val SHOOT_ACTION = "shoot"
const val RELOAD_ACTION = "reload"

class PlayerWeaponSystem(
    private val world: World,
    private val input: Input,
    private val renderer: Renderer,
    private val events: Events,
    private val weaponsModule: WeaponsModule
) : System {

    private var previousTriggerState = false

    override fun update(deltaTime: Float) {
        world.entities.each { entity: Entity, _: Player ->
            update(entity)
        }
    }

    private fun update(entity: Entity) {
        val triggerState = input.poll(SHOOT_ACTION) == 1f

        val shootPos = entity.pos
        applyRangedWeaponOffset(entity, shootPos)

        val target = toWorld(renderer.pointer.pos, entity.z)
        applyRangedWeaponOffset(entity, target)

        drawDebug(shootPos, target)

        if (canShoot(entity, triggerState)) {
            shoot(entity, target)
        }

        input.onAction(RELOAD_ACTION) {
            events.fire(WeaponsModule.ReloadEvent(entity))
        }

        previousTriggerState = triggerState
    }

    private fun canShoot(entity: Entity, triggerState: Boolean): Boolean {
        val weapon = weaponsModule.getSelectedWeapon(entity)
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

    private fun shoot(entity: Entity, target: Vector3) {
        events.fire(WeaponsModule.ShootEvent(entity, target))

        val weapon = weaponsModule.getSelectedWeapon(entity)
        if (weapon is RangedWeapon && weapon.ammo <= 0f) {
            events.fire(WeaponsModule.ReloadEvent(entity))
        }
    }

    private fun drawDebug(shootPos: Vector3, target: Vector3) {
        renderer.debug.category("weapon").addLine(
            shootPos,
            target,
            1.25f,
            Color.CYAN
        )

        renderer.debug.category("weapon").addLine(
            target,
            Vector3(target.x, target.y, 0f),
            1.25f,
            Color.CYAN
        )
    }
}
