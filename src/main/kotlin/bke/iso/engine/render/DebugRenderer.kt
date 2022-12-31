package bke.iso.engine.render

import bke.iso.app.service.Service
import bke.iso.engine.*
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.physics.CollisionService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Polygon

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
            val positionMarker = toScreen(Circle(location.toVector2(), 1f))
            drawCircle(positionMarker, Color.CYAN)
        }

        for (entity in entityService.getAll()) {
            val positionMarker = toScreen(Circle(entity.x, entity.y, 2f))
            drawCircle(positionMarker, Color.RED)
            drawCollisionBoxes(entity)
            drawDebugData(entity)
        }

        Gdx.gl.glDisable(GL30.GL_BLEND)
    }

    private fun drawCollisionBoxes(entity: Entity) {
        val collisionData = collisionService.findCollisionData(entity) ?: return
        val polygon = toScreen(collisionData.box)
        drawPolygon(polygon, Color.GREEN)
    }

    private fun drawDebugData(entity: Entity) {
        val debugData = entity.get<DebugData>() ?: return

        for (line in debugData.lines) {
            shapeRenderer.color = line.color
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.line(toScreen(line.start), toScreen(line.end))
            shapeRenderer.end()
        }

        for (debugCircle in debugData.circles) {
            val radius = debugCircle.radius
            val ratio = getIsometricRatio()
            val width = radius * TILE_WIDTH * ratio
            val height = radius * TILE_HEIGHT * ratio
            val pos = toScreen(entity.x, entity.y)
            shapeRenderer.color = debugCircle.color
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.ellipse(pos.x - (width/2), pos.y - (height/2), width, height)
            shapeRenderer.end()
        }

        for (point in debugData.points) {
            val circle = Circle(point.pos, point.size)
            drawCircle(toScreen(circle), point.color)
        }
    }

    // TODO: since this is filled, rename to drawPoint
    private fun drawCircle(circle: Circle, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(circle.x, circle.y, circle.radius)
        shapeRenderer.end()
    }

    private fun drawPolygon(polygon: Polygon, color: Color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.polygon(polygon.transformedVertices)
        shapeRenderer.end()
    }

    fun clearDebugData() {
        for (entity in entityService.getAll()) {
            entity.remove<DebugData>()
        }
    }
}
