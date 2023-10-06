package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.editor.EditorTool
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import mu.KotlinLogging
import kotlin.math.floor

class BrushTool(
    private val world: World,
    private val renderer: Renderer
) : EditorTool {

    private val log = KotlinLogging.logger {}

    private val referenceSprite = Sprite()
    private val referenceActor = world.actors.create(0f, 0f, 0f, referenceSprite)
    private var selection: Selection? = null

    override fun update() {
        // TODO: scale position when screen size changes
        val pos = toWorld(renderer.getCursorPos())

        if (selection is TileSelection) {
            pos.set(floor(pos.x), floor(pos.y), floor(pos.z))
        }
        referenceActor.moveTo(pos.x, pos.y, pos.z)
        renderer.fgShapes.addBox(getBox(), 1f, Color.GREEN)
    }

    private fun getBox() =
        if (selection is TileSelection) {
            Box.fromMinMax(
                referenceActor.pos,
                referenceActor.pos.add(1f, 1f, 0f)
            )
        } else {
            referenceActor
                .getCollisionBox()
                ?: Box.fromMinMax(
                    referenceActor.pos,
                    referenceActor.pos.add(1f, 1f, 1f)
                )
        }

    override fun performAction(): EditorCommand? =
        when (val s = selection) {
            is TileSelection -> CreateTileCommand(world, s.prefab, Location(referenceActor.pos))
            is ActorSelection -> CreateActorCommand(world, s.prefab, referenceActor.pos)
            else -> null
        }

    override fun enable() {
        referenceSprite.alpha = 1f
    }

    override fun disable() {
        referenceSprite.alpha = 0f
    }

    fun selectPrefab(prefab: TilePrefab) {
        log.debug { "tile prefab '${prefab.name}' selected" }
        selection = TileSelection(prefab)

        referenceSprite.texture = prefab.texture
        referenceSprite.offsetX = 0f
        referenceSprite.offsetY = 16f
        referenceSprite.scale = 1f
        // only need colliders when placing actors
        referenceActor.remove<Collider>()
    }

    fun selectPrefab(prefab: ActorPrefab) {
        log.debug { "actor prefab '${prefab.name}' selected" }
        selection = ActorSelection(prefab)

        prefab.components.withFirstInstance<Sprite> { sprite ->
            referenceSprite.texture = sprite.texture
            referenceSprite.offsetX = sprite.offsetX
            referenceSprite.offsetY = sprite.offsetY
            referenceSprite.scale = sprite.scale
        }

        prefab.components.withFirstInstance<Collider> { collider ->
            referenceActor.add(collider.copy())
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
