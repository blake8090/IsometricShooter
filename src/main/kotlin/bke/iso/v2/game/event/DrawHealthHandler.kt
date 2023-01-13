package bke.iso.v2.game.event

import bke.iso.service.Transient
import bke.iso.v2.engine.asset.AssetService
import bke.iso.v2.engine.event.EventHandler
import bke.iso.v2.engine.math.toScreen
import bke.iso.v2.engine.render.DrawEntityEvent
import bke.iso.v2.game.Health
import bke.iso.v2.game.HealthBar
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

@Transient
class DrawHealthHandler(private val assetService: AssetService) : EventHandler<DrawEntityEvent> {
    private val healthBarWidth = 32f
    private val healthBarHeight = 8f

    override val type = DrawEntityEvent::class

    override fun handle(event: DrawEntityEvent) {
        val entity = event.entity
        val health = entity.get<Health>() ?: return
        val healthBar = entity.get<HealthBar>() ?: return

        val batch = event.batch
        val pixel = assetService.get<Texture>("pixel") ?: return
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
