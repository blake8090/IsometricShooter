package bke.iso.game.combat

import bke.iso.engine.asset.AssetService
import bke.iso.engine.event.EventHandler
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.DrawEntityEvent
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

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
        val offset = Vector2(healthBar.offsetX, healthBar.offsetY)
        val pos = toScreen(entity.x, entity.y, entity.z).sub(offset)

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

private fun PolygonSpriteBatch.withColor(color: Color, action: (PolygonSpriteBatch) -> Unit) {
    val originalColor = Color(this.color)
    this.color = color
    action.invoke(this)
    this.color = originalColor
}
