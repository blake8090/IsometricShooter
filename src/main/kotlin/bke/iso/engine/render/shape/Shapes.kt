package bke.iso.engine.render.shape

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool.Poolable

sealed class Shape3D

data class Line3D(
    var start: Vector3 = Vector3(),
    var end: Vector3 = Vector3(),
    var width: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, Shape3D() {

    override fun reset() {
        start.setZero()
        end.setZero()
        width = 0f
        color = Color.WHITE
    }
}

data class Circle3D(
    var pos: Vector3 = Vector3(),
    var radius: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, Shape3D() {

    override fun reset() {
        pos.setZero()
        radius = 0f
        color = Color.WHITE
    }
}

data class Point3D(
    var pos: Vector3 = Vector3(),
    var size: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, Shape3D() {

    override fun reset() {
        pos.setZero()
        size = 0f
        color = Color.WHITE
    }
}

data class Sphere3D(
    var pos: Vector3 = Vector3(),
    var radius: Float = 0f,
    var color: Color = Color.WHITE
) : Poolable, Shape3D() {

    override fun reset() {
        pos.setZero()
        radius = 0f
        color = Color.WHITE
    }
}
