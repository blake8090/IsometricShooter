package bke.iso.engine.render

//import bke.iso.engine.entity.Entities
//import bke.iso.engine.entity.Entity
//import bke.iso.engine.entity.Sprite
import bke.iso.engine.Tiles
import bke.iso.engine.Units
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle

class DebugRenderer(
    private val tiles: Tiles,
//    private val entities: Entities
) {
    private val shapeRenderer = ShapeRenderer()

    fun render(camera: OrthographicCamera) {
//        shapeRenderer.projectionMatrix = camera.combined
//
//        Gdx.gl.glEnable(GL30.GL_BLEND)
//        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
//
//        tiles.forEachTile { location, _ ->
//            val circle = Circle(location.x.toFloat(), location.y.toFloat(), 3f)
//            drawWorldCircle(circle, Color.CYAN)
//        }
//
//        for (entity in entities) {
//            val circle = Circle(entity.getPos(), 3f)
//            drawWorldCircle(circle, Color.RED)
//            drawCollisionAreas(entity)
//        }
//
//        Gdx.gl.glDisable(GL30.GL_BLEND)
    }

//    private fun drawCollisionAreas(entity: Entity) {
//        val frameData = entity.getComponent<CollisionFrameData>() ?: return
//        drawWorldRectangle(frameData.collisionArea, Color.GREEN)
//        frameData.projectedAreaX
//            ?.let { area -> drawWorldRectangle(area, Color.RED) }
//        frameData.projectedAreaY
//            ?.let { area -> drawWorldRectangle(area, Color.RED) }
//    }

    private fun drawWorldCircle(circle: Circle, color: Color) {
        val pos = Units.worldToScreen(circle.x, circle.y)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(pos.x, pos.y, circle.radius)
        shapeRenderer.end()
    }

    private fun drawWorldRectangle(rectangle: Rectangle, color: Color) {
        val bottomLeft = Units.worldToScreen(
            rectangle.x,
            rectangle.y
        )
        val bottomRight = Units.worldToScreen(
            rectangle.x + rectangle.width,
            rectangle.y
        )
        val topLeft = Units.worldToScreen(
            rectangle.x,
            rectangle.y + rectangle.height
        )
        val topRight = Units.worldToScreen(
            rectangle.x + rectangle.width,
            rectangle.y + rectangle.height
        )

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.line(topLeft, topRight)
        shapeRenderer.line(bottomLeft, bottomRight)
        shapeRenderer.line(topLeft, bottomLeft)
        shapeRenderer.line(topRight, bottomRight)
        shapeRenderer.end()
    }
}
