package bke.iso.editor.scene.tool

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.editor.scene.command.PaintEntityCommand
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.asset.entity.has
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.math.floor
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Tile
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
    private var selectedTemplate: EntityTemplate? = null

    override fun update() {
    }

    override fun draw() {
        renderer.fgShapes.addBox(getBox(), 1f, Color.GREEN)

        val pos = Vector3(pointerPos)

        val isTileTemplate = selectedTemplate
            ?.has<Tile>()
            ?: false
        if (isTileTemplate || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
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
        brushEntity
            .getCollisionBox()
            ?: Box.fromMinMax(
                brushEntity.pos,
                brushEntity.pos.add(1f, 1f, 1f)
            )

    override fun performAction(): EditorCommand? {
        val template = selectedTemplate
        return if (template == null) {
            null
        } else if (worldLogic.getTileTemplateName(Location(brushEntity.pos)) == template.name) {
            // avoid replacing tiles with the same template
            null
        } else {
            PaintEntityCommand(worldLogic, template, brushEntity.pos)
        }
    }

    override fun performMultiAction(): EditorCommand? {
        val template = selectedTemplate
        // only tiles are supported for multi actions
        return if (template == null || !template.has<Tile>()) {
            null
        }
        // avoid replacing tiles with the same template
        else if (worldLogic.getTileTemplateName(Location(brushEntity.pos)) == template.name) {
            null
        } else {
            PaintEntityCommand(worldLogic, template, brushEntity.pos)
        }
    }

    override fun performReleaseAction(): EditorCommand? = null

    override fun enable() {
        brushSprite.alpha = 1f
    }

    override fun disable() {
        brushSprite.texture = ""
        brushSprite.alpha = 0f
        selectedTemplate = null
    }

    fun selectTemplate(template: EntityTemplate) {
        log.debug { "entity template '${template.name}' selected" }
        selectedTemplate = template

        template.components.withFirstInstance<Sprite> { sprite ->
            brushSprite.texture = sprite.texture
            brushSprite.offsetX = sprite.offsetX
            brushSprite.offsetY = sprite.offsetY
            brushSprite.scale = sprite.scale
        }

        // visualization of entity's collision box
        template.components.withFirstInstance<Collider> { collider ->
            brushEntity.add(collider.copy())
        }
    }
}
