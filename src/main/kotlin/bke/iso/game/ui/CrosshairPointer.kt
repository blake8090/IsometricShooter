package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.render.Pointer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

private const val CONTROLLER_DEADZONE = 0.1f

class CrosshairPointer(
    private val assets: Assets,
    private val input: Input
) : Pointer() {

    private lateinit var texture: Texture
    private lateinit var offset: Vector2
    private var visible = true

    override fun create() {
        texture = assets.get<Texture>("cursor.png")
        offset = Vector2(texture.width / 2f, texture.height / 2f)
    }

    override fun show() {
    }

    override fun hide() {
    }

    override fun update(deltaTime: Float) {
        visible = true

        if (!input.isUsingController()) {
            pos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            return
        }

        val direction = input.pollAxes(actionX = "cursorX", actionY = "cursorY", CONTROLLER_DEADZONE)
        if (direction.isZero) {
            visible = false
        }

        val center = Vector2(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        pos.set(center)

        val width = Gdx.graphics.width / 2f * 0.7f
        val height = Gdx.graphics.height / 2f * 0.7f
        pos.add(direction.x * width, direction.y * height)
    }

    override fun draw(batch: PolygonSpriteBatch, screenPos: Vector2) {
        if (visible) {
            screenPos.sub(offset)
            batch.begin()
            batch.draw(texture, screenPos.x, screenPos.y)
            batch.end()
        }
    }
}
