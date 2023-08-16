package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.render.CustomCursor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

class CrosshairCursor(assets: Assets) : CustomCursor() {

    private val texture = assets.get<Texture>("cursor")
    private val offset = Vector2(texture.width / 2f, texture.height / 2f)

    override fun update(deltaTime: Float) {
    }

    override fun draw(batch: PolygonSpriteBatch, unProjectedPos: Vector2) {
        unProjectedPos.sub(offset)
        batch.begin()
        batch.draw(texture, unProjectedPos.x, unProjectedPos.y)
        batch.end()
    }
}
