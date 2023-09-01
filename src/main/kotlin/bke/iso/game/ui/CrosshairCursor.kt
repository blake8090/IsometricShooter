package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.render.CustomCursor
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

private const val MAX_SCREEN_RATIO = 0.5f
private const val MINIMUM_AXIS_VALUE = 0.1f

class CrosshairCursor(
    private val assets: Assets,
    private val input: Input
) : CustomCursor() {

    private lateinit var texture: Texture
    private lateinit var offset: Vector2
    private val pos = Vector2()
    private var hide = false

    override fun getPos(): Vector2 = pos

    override fun create() {
        texture = assets.get<Texture>("game/gfx/cursor")
        offset = Vector2(texture.width / 2f, texture.height / 2f)
    }

    override fun update(deltaTime: Float) {
        if (!input.isUsingController()) {
            pos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            hide = false
            return
        }

        val width = Gdx.graphics.width / 2f
        val height = Gdx.graphics.height / 2f
        pos.set(width, height)

        val axis = Vector2(
            input.controller.poll("cursorX", 0f),
            input.controller.poll("cursorY", 0f)
        )
        if (axis.len() <= MINIMUM_AXIS_VALUE) {
            hide = true
            return
        }
        axis.clamp(0f, MAX_SCREEN_RATIO)
        pos.add(width * axis.x, height * axis.y)
        hide = false
    }

    override fun draw(batch: PolygonSpriteBatch, unProjectedPos: Vector2) {
        if (hide) {
            return
        }
        unProjectedPos.sub(offset)
        batch.begin()
        batch.draw(texture, unProjectedPos.x, unProjectedPos.y)
        batch.end()
    }
}
