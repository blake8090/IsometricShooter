package bke.iso.game.hud

import bke.iso.engine.core.Event
import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Module
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.entity.OptimizedEntityRenderer
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.render.withColor
import bke.iso.engine.ui.UI
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.World
import bke.iso.game.entity.Inventory
import bke.iso.game.combat.CombatModule
import bke.iso.game.combat.system.Health
import bke.iso.game.combat.system.HealthBar
import bke.iso.game.entity.player.Player
import bke.iso.game.weapon.system.RangedWeapon
import bke.iso.game.weapon.RangedWeaponProperties
import bke.iso.game.weapon.system.Weapon
import bke.iso.game.weapon.WeaponProperties
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

class HudModule(
    private val world: World,
    private val assets: Assets,
    private val weaponsModule: WeaponsModule,
) : Module {

    override val alwaysActive: Boolean = false

    private val view = HudView(assets)

    fun init(ui: UI) {
        ui.pushView(view)
    }

    fun updateHealthBar(health: Float, maxHealth: Float) {
        view.setHealth(health)
        view.setMaxHealth(maxHealth)
    }

    fun hideInteractionText() {
        view.hideInteractionText()
    }

    fun setInteractionText(text: String) {
        view.setInteractionText(text)
    }

    override fun update(deltaTime: Float) {
        val text =
            when (val weapon = findPlayerWeapon()) {
                null -> "No weapon"
                is RangedWeapon -> getRangedWeaponText(weapon)
                else -> weapon.name
            }
        view.setWeaponText(text)

        updateMedkitText()
    }

    private fun findPlayerWeapon(): Weapon? {
        val playerEntity = world.entities.find<Player>() ?: return null
        return weaponsModule.getSelectedWeapon(playerEntity)
    }

    private fun getRangedWeaponText(weapon: RangedWeapon): String {
        val properties = assets.get<WeaponProperties>(weapon.name) as RangedWeaponProperties

        val builder = StringBuilder()
        builder.append(weapon.name)

        if (weapon.reloadCoolDown > 0f) {
            builder.append(": reloading")
        } else {
            builder.append(": ${weapon.ammo}/${properties.magSize}")
        }

        return builder.toString()
    }

    private fun updateMedkitText() {
        val inventory = world
            .entities
            .find<Player>()
            ?.get<Inventory>()
            ?: return

        view.setMedkitsText(inventory.numMedkits)
    }

    override fun handleEvent(event: Event) {
        if (event is CombatModule.PlayerHealthChangeEvent) {
            view.setHealth(event.health)
        } else if (event is OptimizedEntityRenderer.DrawEntityEvent) {
            drawHealthBar(event.entity, event.batch)
        }
    }

    private fun drawHealthBar(entity: Entity, batch: PolygonSpriteBatch) {
        // we already display the player's health in the HUD!
        if (entity.has<Player>()) {
            return
        }

        val healthBarWidth = 32f
        val healthBarHeight = 8f

        val health = entity.get<Health>() ?: return
        val healthBar = entity.get<HealthBar>() ?: return

        val pixel = makePixelTexture()
        val pos = toScreen(entity.pos)
            .sub(healthBar.offsetX, healthBar.offsetY)

        batch.withColor(Color.RED) {
            batch.draw(pixel, pos.x, pos.y, healthBarWidth, healthBarHeight)
        }

        batch.withColor(Color.GREEN) {
            val ratio = health.value / health.maxValue
            val width = healthBarWidth * ratio
            batch.draw(pixel, pos.x, pos.y, width, healthBarHeight)
        }
    }
}
