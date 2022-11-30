package bke.iso.engine.render

import bke.iso.app.service.Service
import bke.iso.engine.*
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.physics.CollisionService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle

@Service
class DebugRenderer(
    private val tiles: Tiles,
    private val entityService: EntityService,
    private val collisionService: CollisionService
) {
    private val shapeRenderer = ShapeRenderer()

    fun render(camera: OrthographicCamera) {
        shapeRenderer.projectionMatrix = camera.combined

        Gdx.gl.glEnable(GL30.GL_BLEND)
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)

        tiles.forEachTile { location, _ ->
            val circle = Circle(location.x.toFloat(), location.y.toFloat(), 3f)
            drawWorldCircle(circle, Color.CYAN)
        }

        for (entity in entityService.getAll()) {
            val circle = Circle(entity.x, entity.y, 3f)
            drawWorldCircle(circle, Color.RED)
            drawCollisionAreas(entity)
        }

        Gdx.gl.glDisable(GL30.GL_BLEND)
    }

    private fun drawCollisionAreas(entity: Entity) {
        val collisionData = collisionService.findCollisionData(entity) ?: return
        val polygon = Units.toScreen(collisionData.area)
        drawPolygon(polygon, Color.GREEN)
    }

    private fun drawWorldCircle(circle: Circle, color: Color) {
        val pos = Units.worldToScreen(circle.x, circle.y)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(pos.x, pos.y, circle.radius)
        shapeRenderer.end()
    }

    private fun drawPolygon(polygon: Polygon, color: Color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.polygon(polygon.transformedVertices)
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
