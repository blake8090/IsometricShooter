package bke.iso.engine.render.debug

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool.Poolable

data class DebugLine(
    var start: Vector3 = Vector3(),
    var end: Vector3 = Vector3(),
    var width: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable {

    override fun reset() {}
}

data class DebugRectangle(
    var rectangle: Rectangle = Rectangle(),
    // TODO: implement line width
    var lineWidth: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable {

    override fun reset() {}
}

data class DebugCircle(
    var pos: Vector3 = Vector3(),
    var radius: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable {

    override fun reset() {}
}

data class DebugPoint(
    var pos: Vector3 = Vector3(),
    var size: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable {

    override fun reset() {}
}

data class DebugBox(
    var pos: Vector3 = Vector3(),
    var dimensions: Vector3 = Vector3(),
    var color: Color = Color.WHITE
) : Poolable {

    override fun reset() {}
}
