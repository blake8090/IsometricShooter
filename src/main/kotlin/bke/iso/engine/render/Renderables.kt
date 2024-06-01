package bke.iso.engine.render

import bke.iso.engine.math.Box
import bke.iso.engine.world.GameObject
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool.Poolable

//open class TextureRenderable(
//    var textureRegion: TextureRegion? = null,
//    var x: Float = 0f,
//    var y: Float = 0f,
//    var offsetX: Float = 0f,
//    var offsetY: Float = 0f,
//    var width: Int = 0,
//    var height: Int = 0,
//    var alpha: Float = 0f,
//    var rotation: Float = 0f
//) : Poolable {
//
//    override fun reset() {
//        textureRegion = null
//        x = 0f
//        y = 0f
//        offsetX = 0f
//        offsetY = 0f
//        width = 0
//        height = 0
//        alpha = 0f
//        rotation = 0f
//    }
//}

class TextRenderable(
    var font: BitmapFont? = null,
    var text: String = "",
    var color: Color = Color.WHITE,
    var x: Float = 0f,
    var y: Float = 0f,
) : Poolable {

    override fun reset() {
        font = null
        text = ""
        color = Color.WHITE
        x = 0f
        y = 0f
    }
}

open class GameObjectRenderable(
    var gameObject: GameObject? = null,
    var texture: Texture? = null,
    var bounds: Box? = null,
    var x: Float = 0f,
    var y: Float = 0f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var alpha: Float = 0f,
    var rotation: Float = 0f,
    var color: Color? = null
) : Poolable {

    val behind = Array<GameObjectRenderable>()
    var visited = false

    override fun reset() {
        gameObject = null
        texture = null
        bounds = null
        x = 0f
        y = 0f
        offsetX = 0f
        offsetY = 0f
        width = 0f
        height = 0f
        alpha = 0f
        rotation = 0f
        color = null

        behind.clear()
        visited = false
    }
}
