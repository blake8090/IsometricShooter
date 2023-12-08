package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.render.Pointer
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import bke.iso.game.player.Player
import bke.iso.game.weapon.Inventory
import bke.iso.game.weapon.RangedWeaponItem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import kotlin.math.min

private const val CONTROLLER_DEADZONE = 0.1f
private const val SCREEN_RATIO = 0.7f

class CrosshairPointer(
    private val assets: Assets,
    private val input: Input,
    private val world: World,
    private val renderer: Renderer
) : Pointer() {

    private lateinit var texture: Texture
    private lateinit var offset: Vector2
    private var visible = true

    override fun create() {
        texture = assets.get<Texture>("crosshair.png")
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

        pos.set(getGraphicsSize().scl(0.5f))
        val movement = getGraphicsSize()
            .scl(0.5f)
            .scl(SCREEN_RATIO)
            .scl(direction)
        pos.add(movement)
    }

    private fun getGraphicsSize() =
        Vector2(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

    override fun draw(batch: PolygonSpriteBatch, screenPos: Vector2) {
        if (!visible) {
            return
        }

        val scale = min(1f + getWeaponRecoil(), 2f)
        batch.begin()
        renderer.drawTexture("crosshair.png", screenPos, offset, scale, 1f)
        batch.end()
    }

    private fun getWeaponRecoil(): Float {
        val weaponItem = world.actors
            .find<Player>()
            ?.get<Inventory>()
            ?.selectedWeapon
        return if (weaponItem is RangedWeaponItem) {
            weaponItem.recoil
        } else {
            0f
        }
    }
}
