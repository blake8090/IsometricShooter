package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.editor.EditorTool
import bke.iso.editor.ReferenceActors
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.floor
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class BrushTool(
    val world: World,
    private val referenceActors: ReferenceActors,
    private val renderer: Renderer
) : EditorTool {

    private val log = KotlinLogging.logger {}

    private val brushSprite = Sprite(alpha = 0f)
    private val brushActor = world.actors.create(0f, 0f, 0f, brushSprite)
    private var selection: Selection? = null

    override fun update() {
        val pos = getNewPos()
        brushActor.moveTo(pos.x, pos.y, pos.z)
        renderer.fgShapes.addBox(getBox(), 1f, Color.GREEN)
    }

    private fun getNewPos(): Vector3 {
        // TODO: scale position when screen size changes
        val pos = toWorld(renderer.getCursorPos())

        if (selection is TileSelection || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            pos.floor()
        }

        return pos
    }

    private fun getBox() =
        if (selection is TileSelection) {
            Box.fromMinMax(
                brushActor.pos,
                brushActor.pos.add(1f, 1f, 0f)
            )
        } else {
            brushActor
                .getCollisionBox()
                ?: Box.fromMinMax(
                    brushActor.pos,
                    brushActor.pos.add(1f, 1f, 1f)
                )
        }

    override fun performAction(): EditorCommand? =
        when (val s = selection) {
            is TileSelection -> paintTile(s.prefab, Location(brushActor.pos))
            is ActorSelection -> PaintActorCommand(referenceActors, s.prefab, brushActor.pos)
            else -> null
        }

    private fun paintTile(prefab: TilePrefab, location: Location): PaintTileCommand? =
        if (!referenceActors.hasTile(location)) {
            PaintTileCommand(referenceActors, prefab, location)
        } else {
            null
        }

    override fun enable() {
        brushSprite.alpha = 1f
    }

    override fun disable() {
        brushSprite.alpha = 0f
    }

    fun selectPrefab(prefab: TilePrefab) {
        log.debug { "tile prefab '${prefab.name}' selected" }
        selection = TileSelection(prefab)

        brushSprite.texture = prefab.texture
        brushSprite.offsetX = 0f
        brushSprite.offsetY = 16f
        brushSprite.scale = 1f
        // only need colliders when placing actors
        brushActor.remove<Collider>()
    }

    fun selectPrefab(prefab: ActorPrefab) {
        log.debug { "actor prefab '${prefab.name}' selected" }
        selection = ActorSelection(prefab)

        prefab.components.withFirstInstance<Sprite> { sprite ->
            brushSprite.texture = sprite.texture
            brushSprite.offsetX = sprite.offsetX
            brushSprite.offsetY = sprite.offsetY
            brushSprite.scale = sprite.scale
        }

        prefab.components.withFirstInstance<Collider> { collider ->
            brushActor.add(collider.copy())
        }
    }

    private sealed class Selection

    private class TileSelection(val prefab: TilePrefab) : Selection()

    private class ActorSelection(val prefab: ActorPrefab) : Selection()
}

// TODO: replace other filterIsInstance -> let usages with this
private inline fun <reified T : Any> Collection<*>.withFirstInstance(action: (T) -> Unit) =
    filterIsInstance<T>()
        .firstOrNull()
        ?.let { instance -> action.invoke(instance) }
