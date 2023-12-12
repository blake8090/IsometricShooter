package bke.iso.game.hud

import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.asset.Assets
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.DrawActorEvent
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.render.withColor
import bke.iso.engine.ui.UI
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import bke.iso.game.combat.Health
import bke.iso.game.combat.HealthBar
import bke.iso.game.combat.PlayerDamageEvent
import bke.iso.game.player.PLAYER_MAX_HEALTH
import bke.iso.game.player.Player
import bke.iso.game.weapon.Inventory
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

class HudModule(
    private val world: World,
    assets: Assets
) : Module {

    private val hudScreen = HudScreen(assets)

    fun init(ui: UI, health: Float, maxHealth: Float) {
        ui.setScreen(hudScreen)
        hudScreen.updateHealth(health, maxHealth)
    }

    override fun update(deltaTime: Float) {
        val weaponItem = world.actors
            .find<Player>()
            ?.get<Inventory>()
            ?.selectedWeapon
            ?: return
        hudScreen.updateWeaponText(weaponItem)
    }

    override fun handleEvent(event: Event) {
        if (event is PlayerDamageEvent) {
            hudScreen.updateHealth(event.health, PLAYER_MAX_HEALTH)
        } else if (event is DrawActorEvent) {
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
