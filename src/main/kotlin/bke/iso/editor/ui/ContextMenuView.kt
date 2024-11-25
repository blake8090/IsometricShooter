package bke.iso.editor.ui

import bke.iso.editor.CloseContextMenuEvent
import bke.iso.editor.ContextMenuSelection
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import io.github.oshai.kotlinlogging.KotlinLogging

private const val CONTEXT_MENU_ACTOR_NAME = "contextMenu"

class ContextMenuView(private val skin: Skin) {

    private val log = KotlinLogging.logger {}

    fun create(x: Float, y: Float, selections: Set<ContextMenuSelection>): Actor {
        val root = Table().top().left()
        root.name = CONTEXT_MENU_ACTOR_NAME

        val menu = Table()
        menu.background = skin.getDrawable("bg")
        menu.touchable = Touchable.enabled

        for (selection in selections) {
            addButton(menu, selection)
        }

        root.add(menu).expand()
        root.setPosition(x, y)

        return root
    }

    private fun addButton(menu: Table, selection: ContextMenuSelection) {
        menu.row()

        val button = TextButton(selection.text, skin).apply {
            padLeft(5f)
            padRight(5f)
            label.setAlignment(Align.left)

            onChanged {
                selection.action.invoke()
                fire(CloseContextMenuEvent())
            }
        }

        menu.add(button)
            .grow()
            .space(5f)
    }

    fun touchedContextMenu(stage: Stage): Boolean {
        val screenPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        val stagePos: Vector2 = stage.screenToStageCoordinates(screenPos)

        val actor = stage.hit(stagePos.x, stagePos.y, false) ?: return false
        log.debug { "hit actor ${actor::class.simpleName} ${actor.name}" }

        return searchParents(actor)
    }

    private fun searchParents(actor: Actor): Boolean {
        var currentActor = actor

        while (currentActor.hasParent()) {
            if (currentActor.parent.name == CONTEXT_MENU_ACTOR_NAME) {
                return true
            }

            currentActor = currentActor.parent
        }

        return false
    }
}
