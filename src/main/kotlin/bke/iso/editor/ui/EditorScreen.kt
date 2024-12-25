package bke.iso.editor.ui

import bke.iso.editor.ContextMenuSelection
import bke.iso.editor.EditorEvent
import bke.iso.editor.EditorEventListener
import bke.iso.editor.EditorState
import bke.iso.editor.SelectTabEvent
import bke.iso.editor.actor.ui.ActorTabView
import bke.iso.editor.scene.ui.SceneTabView
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Window
import io.github.oshai.kotlinlogging.KotlinLogging

enum class Tab {
    SCENE,
    ACTOR,
    NONE
}

class EditorScreen(
    private val editorState: EditorState,
    assets: Assets
) : UIScreen(assets) {

    private val log = KotlinLogging.logger { }

    private val root = Table().top()

    val sceneTabView = SceneTabView(skin, assets, stage)
    val actorTabView = ActorTabView(skin, assets)

    private val contextMenuView = ContextMenuView(skin, assets)
    private var contextMenuActor: Actor? = null

    var activeTab: Tab = Tab.NONE
        private set

    override fun create() {
        setup()

        sceneTabView.create()
        actorTabView.create()

        root.setFillParent(true)

        val menuBarStack = Stack()
        menuBarStack.add(sceneTabView.menuBar)
        menuBarStack.add(actorTabView.menuBar)
        root.add(menuBarStack)
            .growX()
            .left()

        root.row()
        root.add(createTabs())
            .growX()
            .top()
            .left()

        root.row()

        val mainViewStack = Stack()
        mainViewStack.add(sceneTabView.mainView)
        mainViewStack.add(actorTabView.mainView)
        root.add(mainViewStack).grow()

        root.addListener(object : EditorEventListener {
            override fun handle(event: EditorEvent) {
                editorState.handleEditorEvent(event)
            }
        })

        stage.addActor(root)
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("bg", makePixelTexture(color(10, 23, 36)))
        skin.add("table-border", color(77, 100, 130))

        skin.add("default", assets.fonts[FontOptions("roboto.ttf", 14f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.getDrawable("bg")
        })

        skin.add("default", TextField.TextFieldStyle().apply {
            font = skin.getFont("default")
            fontColor = Color.WHITE
            focusedFontColor = Color.WHITE

            background = skin.newDrawable("pixel", Color.BLACK)
            focusedBackground = skin.newDrawable("pixel", Color.BLACK)

            cursor = skin.newDrawable("pixel", color(50, 158, 168))
            selection = skin.newDrawable("pixel", color(50, 158, 168))
        })

        skin.add("default", Window.WindowStyle().apply {
            background = skin.getDrawable("bg")
            titleFont = skin.getFont("default")
        })

        setupButtonStyles()

        skin.add("default", SelectBoxStyle().apply {
            background = skin.newTintedDrawable("pixel", "button-over")

            scrollStyle = ScrollPane.ScrollPaneStyle().apply {
                font = skin.getFont("default")
            }

            listStyle = List.ListStyle().apply {
                font = skin.getFont("default")
                selection = skin.newTintedDrawable("pixel", "button-over")
                over = skin.newTintedDrawable("pixel", "button-over")
            }
        })
    }

    private fun setupButtonStyles() {
        skin.add("button-up", color(20, 51, 82))
        skin.add("button-over", color(34, 84, 133))
        skin.add("button-down", color(43, 103, 161))
        skin.add("button-checked", color(43, 103, 161))

        skin.add("default", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            down = skin.newTintedDrawable("pixel", "button-down")
            over = skin.newTintedDrawable("pixel", "button-over")
        })

        skin.add("checkable", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            down = skin.newTintedDrawable("pixel", "button-down")
            over = skin.newTintedDrawable("pixel", "button-over")
            checked = skin.newTintedDrawable("pixel", "button-checked")
        })
    }

    private fun createTabs(): Table {
        val tabs = Table().left()
        tabs.background = skin.getDrawable("bg")

        val sceneButton = createButton("Scene")
        sceneButton.onChanged {
            if (sceneButton.isChecked) {
                selectTab(Tab.SCENE)
            }
        }

        val actorButton = createButton("Actor")
        actorButton.onChanged {
            if (actorButton.isChecked) {
                selectTab(Tab.ACTOR)
            }
        }

        tabs.add(sceneButton)
        tabs.add(actorButton)

        ButtonGroup<TextButton>().add(sceneButton, actorButton)

        return tabs
    }

    private fun createButton(text: String): TextButton {
        val vPad = 10f
        val hPad = 10f
        return TextButton(text, skin, "checkable").apply {
            padTop(vPad)
            padBottom(vPad)
            padLeft(hPad)
            padRight(hPad)
        }
    }

    private fun selectTab(tab: Tab) {
        log.info { "Selected tab $tab" }
        activeTab = tab

        if (tab == Tab.SCENE) {
            sceneTabView.menuBar.isVisible = true
            sceneTabView.mainView.isVisible = true

            actorTabView.menuBar.isVisible = false
            actorTabView.mainView.isVisible = false

            root.fire(SelectTabEvent(Tab.SCENE))
        } else if (tab == Tab.ACTOR) {
            sceneTabView.menuBar.isVisible = false
            sceneTabView.mainView.isVisible = false

            actorTabView.menuBar.isVisible = true
            actorTabView.mainView.isVisible = true

            root.fire(SelectTabEvent(Tab.ACTOR))
        }
    }

    fun openContextMenu(pos: Vector2, selections: Set<ContextMenuSelection>) {
        closeContextMenu()

        val stagePos = stage.screenToStageCoordinates(pos)
        val newActor = contextMenuView.create(stagePos.x, stagePos.y, selections)
        // since this is not a child of the root actor, we have to manually add an event listener handler
        newActor.addListener(object : EditorEventListener {
            override fun handle(event: EditorEvent) {
                editorState.handleEditorEvent(event)
            }
        })

        contextMenuActor = newActor
        stage.addActor(contextMenuActor)
    }

    fun closeContextMenu() {
        contextMenuActor?.addAction(Actions.removeActor())
    }

    fun touchedContextMenu() =
        contextMenuView.touchedContextMenu(stage)
}
