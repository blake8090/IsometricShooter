package bke.iso.engine.render.debug

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool.Poolable

sealed class DebugShape

data class DebugLine(
    var start: Vector3 = Vector3(),
    var end: Vector3 = Vector3(),
    var width: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, DebugShape() {

    override fun reset() {
        start = Vector3()
        end = Vector3()
        width = 0f
        color = Color.WHITE
    }
}

data class DebugRectangle(
    var rectangle: Rectangle = Rectangle(),
    var lineWidth: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, DebugShape() {

    override fun reset() {
        rectangle = Rectangle()
        lineWidth = 0f
        color = Color.WHITE
    }
}

data class DebugCircle(
    var pos: Vector3 = Vector3(),
    var radius: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, DebugShape() {

    override fun reset() {
        pos = Vector3()
        radius = 0f
        color = Color.WHITE
    }
}

data class DebugPoint(
    var pos: Vector3 = Vector3(),
    var size: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, DebugShape() {

    override fun reset() {
        pos = Vector3()
        size = 0f
        color = Color.WHITE
    }
}

data class DebugSphere(
    var pos: Vector3 = Vector3(),
    var radius: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, DebugShape() {

    override fun reset() {
        pos = Vector3()
        radius = 0f
        color = Color.WHITE
    }
}
