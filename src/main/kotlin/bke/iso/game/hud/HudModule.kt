package bke.iso.game.hud

import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.asset.Assets
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.GameObjectRenderer
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.render.withColor
import bke.iso.engine.ui.UI
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import bke.iso.game.PLAYER_MAX_HEALTH
import bke.iso.game.combat.CombatModule
import bke.iso.game.combat.Health
import bke.iso.game.combat.HealthBar
import bke.iso.game.player.Player
import bke.iso.game.weapon.RangedWeapon
import bke.iso.game.weapon.RangedWeaponProperties
import bke.iso.game.weapon.Weapon
import bke.iso.game.weapon.WeaponProperties
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

class HudModule(
    private val world: World,
    private val assets: Assets,
    private val weaponsModule: WeaponsModule
) : Module {

    private val hudScreen = HudScreen(assets)

    fun init(ui: UI, health: Float, maxHealth: Float) {
        ui.setScreen(hudScreen)
        hudScreen.updateHealth(health, maxHealth)
    }

    override fun update(deltaTime: Float) {
        val text =
            when (val weapon = findPlayerWeapon()) {
                null -> "No weapon"
                is RangedWeapon -> getRangedWeaponText(weapon)
                else -> weapon.name
            }
        hudScreen.setWeaponText(text)
    }

    private fun findPlayerWeapon(): Weapon? {
        val playerActor = world.actors.find<Player>() ?: return null
        return weaponsModule.getSelectedWeapon(playerActor)
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

    override fun handleEvent(event: Event) {
        if (event is CombatModule.PlayerDamageEvent) {
            hudScreen.updateHealth(event.health, PLAYER_MAX_HEALTH)
        } else if (event is GameObjectRenderer.DrawActorEvent) {
            drawHealthBar(event.actor, event.batch)
        }
    }

    private fun drawHealthBar(actor: Actor, batch: PolygonSpriteBatch) {
        // we already display the player's health in the HUD!
        if (actor.has<Player>()) {
            return
        }

        val healthBarWidth = 32f
        val healthBarHeight = 8f

        val health = actor.get<Health>() ?: return
        val healthBar = actor.get<HealthBar>() ?: return

        val pixel = makePixelTexture()
        val pos = toScreen(actor.pos)
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
