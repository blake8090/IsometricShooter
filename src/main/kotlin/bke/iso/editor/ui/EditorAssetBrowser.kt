package bke.iso.editor.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class EditorAssetBrowser(
    private val skin: Skin,
    private val assets: Assets
) {

    private val actorBrowser = EditorActorBrowser(assets, skin)
    private val tileBrowser = EditorTileBrowser(assets, skin)
    private val stack = Stack()

    fun create(): Actor {
        setup()

        val root = BorderedTable(skin.getColor("table-border")).apply {
            borderSize = 1f
            background = this@EditorAssetBrowser.skin.getDrawable("bg")

            add(createTabs())
                .expandX()
                .padTop(5f)

            row()
            stack.add(actorBrowser.root)
            stack.add(tileBrowser.root)
            add(stack)
                .grow()
                .pad(2f)
        }

        populateBrowsers()
        return root
    }

    private fun setup() {
        skin.add("asset-browser", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.newTintedDrawable("pixel", "button-up")
            down = skin.newTintedDrawable("pixel", "button-down")
            over = skin.newTintedDrawable("pixel", "button-over")
            checked = skin.newTintedDrawable("pixel", "button-checked")
        })
    }

    private fun createTabs(): Table {
        val table = Table()

        val tileButton = TextButton("Tiles", skin, "asset-browser").apply {
            padLeft(Value.percentWidth(.25f, this))
            padRight(Value.percentWidth(.25f, this))
            onChanged {
                tileBrowser.visible = true
                actorBrowser.visible = false
                actorBrowser.unselect()
            }
        }
        table.add(tileButton)

        val actorButton = TextButton("Actors", skin, "asset-browser").apply {
            padLeft(Value.percentWidth(.25f, this))
            padRight(Value.percentWidth(.25f, this))
            onChanged {
                tileBrowser.visible = false
                tileBrowser.unselect()
                actorBrowser.visible = true
            }
        }
        table.add(actorButton)

        ButtonGroup<TextButton>().add(tileButton, actorButton)
        return table
    }

    private fun populateBrowsers() {
        tileBrowser.populate(assets.getAll<TilePrefab>())
        actorBrowser.populate(assets.getAll<ActorPrefab>())
    }

    fun unselectPrefabs() {
        tileBrowser.unselect()
        actorBrowser.unselect()
    }
}
