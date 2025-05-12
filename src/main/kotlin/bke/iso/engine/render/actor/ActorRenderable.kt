package bke.iso.engine.render.actor

import bke.iso.engine.math.Box
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool.Poolable

open class ActorRenderable(
    var actor: Actor? = null,
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
    var tintColor: Color? = null,
    var drawn: Boolean = false
) : Poolable {

    val behind = Array<ActorRenderable>()
    var visited = false

    override fun reset() {
        actor = null
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
        drawn = false
    }
}
