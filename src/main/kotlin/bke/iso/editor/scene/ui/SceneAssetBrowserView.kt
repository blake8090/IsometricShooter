package bke.iso.editor.scene.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class SceneAssetBrowserView(
    private val skin: Skin,
    private val assets: Assets
) {

    private val actorBrowserView = SceneActorBrowserView(assets, skin)
    private val tileBrowserView = SceneTileBrowserView(assets, skin)
    private val stack = Stack()

    fun create(): Actor {
        val root = BorderedTable(skin.getColor("table-border"))
        root.background = skin.getDrawable("bg")

        root.apply {
            add(createTabs())
                .expandX()
                .padTop(5f)

            row()
            stack.add(actorBrowserView.root)
            stack.add(tileBrowserView.root)
            add(stack)
                .grow()
                .pad(2f)
        }

        populateBrowsers()

        return root
    }

    private fun createTabs(): Table {
        val table = Table()

        val tileButton = TextButton("Tiles", skin).apply {
            padLeft(Value.percentWidth(.25f, this))
            padRight(Value.percentWidth(.25f, this))
            onChanged {
                tileBrowserView.visible = true
                actorBrowserView.visible = false
                actorBrowserView.unselect()
            }
        }
        table.add(tileButton)

        val actorButton = TextButton("Actors", skin).apply {
            padLeft(Value.percentWidth(.25f, this))
            padRight(Value.percentWidth(.25f, this))
            onChanged {
                tileBrowserView.visible = false
                tileBrowserView.unselect()
                actorBrowserView.visible = true
            }
        }
        table.add(actorButton)

        ButtonGroup<TextButton>().add(tileButton, actorButton)
        return table
    }

    private fun populateBrowsers() {
        tileBrowserView.populate(assets.getAll<TilePrefab>())
        actorBrowserView.populate(assets.getAll<ActorPrefab>())
    }

    fun unselectPrefabs() {
        tileBrowserView.unselect()
        actorBrowserView.unselect()
    }
}
