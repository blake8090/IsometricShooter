package bke.iso.engine.render.shape

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable

interface ShapeUtil : Disposable {
    fun update()

    fun begin()

    fun end()

    fun drawPoint(worldPos: Vector3, size: Float, color: Color)

    fun drawRectangle(worldRect: Rectangle, color: Color)

    fun drawLine(start: Vector3, end: Vector3, color: Color)

    fun drawCircle(worldPos: Vector3, worldRadius: Float, color: Color)

    fun drawBox(worldPos: Vector3, dimensions: Vector3, color: Color)
}
