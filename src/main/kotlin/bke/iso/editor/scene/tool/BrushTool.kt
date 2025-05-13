package bke.iso.editor.scene.tool

import bke.iso.editor.scene.command.PaintEntityCommand
import bke.iso.editor.scene.command.PaintTileCommand
import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.prefab.EntityPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.math.floor
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
    private val brushEntity = world.entities.create(Vector3(), brushSprite)
    private var selection: Selection? = null

    override fun update() {
    }

    override fun draw() {
        renderer.fgShapes.addBox(getBox(), 1f, Color.GREEN)

        val pos = Vector3(pointerPos)

        if (selection is TileSelection || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            pos.floor()

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                brushEntity.with<Collider> { collider ->
                    pos.y -= collider.size.y
                }
            }

            if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                brushEntity.with<Collider> { collider ->
                    pos.x -= collider.size.x
                }
            }
        }
        brushEntity.moveTo(pos.x, pos.y, pos.z)
    }

    private fun getBox() =
        if (selection is TileSelection) {
            Box.fromMinMax(
                brushEntity.pos,
                brushEntity.pos.add(1f, 1f, 0f)
            )
        } else {
            brushEntity
                .getCollisionBox()
                ?: Box.fromMinMax(
                    brushEntity.pos,
                    brushEntity.pos.add(1f, 1f, 1f)
                )
        }

    override fun performAction(): EditorCommand? =
        when (val s = selection) {
            is TileSelection -> paintTile(s.prefab, Location(brushEntity.pos))
            is EntitySelection -> PaintEntityCommand(worldLogic, s.prefab, brushEntity.pos)
            else -> null
        }

    override fun performMultiAction(): EditorCommand? =
        when (val s = selection) {
            is TileSelection -> paintTile(s.prefab, Location(brushEntity.pos))
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
        // only need colliders when placing entities
        brushEntity.remove<Collider>()
    }

    fun selectPrefab(prefab: EntityPrefab) {
        log.debug { "entity prefab '${prefab.name}' selected" }
        selection = EntitySelection(prefab)

        prefab.components.withFirstInstance<Sprite> { sprite ->
            brushSprite.texture = sprite.texture
            brushSprite.offsetX = sprite.offsetX
            brushSprite.offsetY = sprite.offsetY
            brushSprite.scale = sprite.scale
        }

        prefab.components.withFirstInstance<Collider> { collider ->
            brushEntity.add(collider.copy())
        }
    }

    private sealed class Selection

    private class TileSelection(val prefab: TilePrefab) : Selection()

    private class EntitySelection(val prefab: EntityPrefab) : Selection()
}
