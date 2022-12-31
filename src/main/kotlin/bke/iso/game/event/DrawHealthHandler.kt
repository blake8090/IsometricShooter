package bke.iso.game.event

import bke.iso.engine.assets.Assets
import bke.iso.engine.event.EventHandler
import bke.iso.engine.render.DrawEntityEvent
import bke.iso.engine.toScreen
import bke.iso.game.Health
import bke.iso.game.HealthBar
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class DrawHealthHandler(private val assets: Assets) : EventHandler<DrawEntityEvent> {
    private val healthBarWidth = 32f
    private val healthBarHeight = 8f

    override val type = DrawEntityEvent::class

    override fun handle(event: DrawEntityEvent) {
        val entity = event.entity
        val health = entity.get<Health>() ?: return
        val healthBar = entity.get<HealthBar>() ?: return

        val batch = event.batch
        val pixel = assets.get<Texture>("pixel") ?: return
        val pos = toScreen(entity.x, entity.y).sub(healthBar.offsetX, healthBar.offsetY)

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

private fun SpriteBatch.withColor(color: Color, action: (SpriteBatch) -> Unit) {
    val originalColor = Color(this.color)
    this.color = color
    action.invoke(this)
    this.color = originalColor
}
