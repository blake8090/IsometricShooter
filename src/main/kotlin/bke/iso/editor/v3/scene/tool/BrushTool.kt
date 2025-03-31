package bke.iso.editor.v3.scene.tool

import bke.iso.editor.v3.EditorCommand
import bke.iso.editor.v3.scene.command.PaintActorCommand
import bke.iso.editor.v3.scene.command.PaintTileCommand
import bke.iso.editor.v3.scene.world.WorldLogic
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class BrushTool(
    override val collisions: Collisions,
    world: World,
    private val worldLogic: WorldLogic,
    private val renderer: Renderer
) : BaseTool() {

    private val log = KotlinLogging.logger { }

    private val brushSprite = Sprite(alpha = 0f)
    private val brushActor = world.actors.create(Vector3(), brushSprite)
    private var selection: Selection? = null

    override fun update() {
    }

    override fun draw() {
        renderer.fgShapes.addBox(getBox(), 1f, Color.GREEN)
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
            is ActorSelection -> PaintActorCommand(worldLogic, s.prefab, brushActor.pos)
            else -> null
        }

    override fun performMultiAction(): EditorCommand? =
        when (val s = selection) {
            is TileSelection -> paintTile(s.prefab, Location(brushActor.pos))
            else -> null
        }

    private fun paintTile(prefab: TilePrefab, location: Location): PaintTileCommand? =
        if (prefab.name != worldLogic.getTilePrefabName(location)) {
            PaintTileCommand(worldLogic, prefab, location)
        } else {
            null
        }

    override fun performReleaseAction(): EditorCommand? = null

    override fun enable() {
        brushSprite.alpha = 1f
    }

    override fun disable() {
        brushSprite.texture = ""
        brushSprite.alpha = 0f
        selection = null
    }

    fun selectPrefab(prefab: TilePrefab) {
        log.debug { "tile prefab '${prefab.name}' selected" }
        selection = TileSelection(prefab)

        brushSprite.texture = prefab.sprite.texture
        brushSprite.offsetX = prefab.sprite.offsetX
        brushSprite.offsetY = prefab.sprite.offsetY
        brushSprite.scale = prefab.sprite.scale
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
