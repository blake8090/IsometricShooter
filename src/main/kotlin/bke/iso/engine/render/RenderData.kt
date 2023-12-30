package bke.iso.engine.render

import bke.iso.engine.asset.Assets
import bke.iso.engine.math.toScreen
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

data class RenderData(
    val texture: Texture,
    val pos: Vector2,
    val width: Float,
    val height: Float,
    val alpha: Float,
    val rotation: Float
)

class RenderDataMapper(private val assets: Assets) {

    fun map(gameObject: GameObject): RenderData? =
        when (gameObject) {
            is Actor -> map(gameObject)
            is Tile -> map(gameObject.sprite, gameObject.location.toVector3())
            else -> null
        }

    private fun map(actor: Actor): RenderData? {
        val sprite = actor.get<Sprite>() ?: return null
        return map(sprite, actor.pos)
    }

    fun map(sprite: Sprite, worldPos: Vector3): RenderData {
        val texture = assets.get<Texture>(sprite.texture)

        val offset = Vector2(sprite.offsetX, sprite.offsetY)
        val pos = toScreen(worldPos).sub(offset)

        val width = texture.width * sprite.scale
        val height = texture.height * sprite.scale

        // when scaling textures, make sure texture is still centered on origin point
        if (sprite.scale != 1f) {
            val diffX = texture.width - width
            val diffY = texture.height - height
            pos.add(diffX / 2f, diffY / 2f)
        }

        return RenderData(
            texture,
            pos,
            width,
            height,
            sprite.alpha,
            sprite.rotation
        )
    }
}
