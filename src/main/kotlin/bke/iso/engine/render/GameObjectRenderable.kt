package bke.iso.engine.render

import bke.iso.engine.math.Box
import bke.iso.engine.world.GameObject
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool.Poolable

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
    var fillColor: Color? = null,
    var tintColor: Color? = null
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
        fillColor = null
        tintColor = null

        behind.clear()
        visited = false
    }
}
