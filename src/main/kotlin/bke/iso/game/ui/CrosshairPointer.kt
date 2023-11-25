package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.render.Pointer
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

class CrosshairPointer(
    private val assets: Assets,
    private val input: Input
) : Pointer() {

    private lateinit var texture: Texture
    private lateinit var offset: Vector2

    override fun create() {
        texture = assets.get<Texture>("cursor.png")
        offset = Vector2(texture.width / 2f, texture.height / 2f)
    }

    override fun show() {
    }

    override fun hide() {
    }

    override fun draw(batch: PolygonSpriteBatch, screenPos: Vector2) {
        batch.begin()
        batch.draw(texture, screenPos.x, screenPos.y)
        batch.end()
    }
}
