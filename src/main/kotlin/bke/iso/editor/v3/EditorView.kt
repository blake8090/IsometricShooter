package bke.iso.editor.v3

import bke.iso.editor.ui.color
import bke.iso.editor.v3.actor.ActorTabView
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.v2.UIView
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Window

class EditorView(
    private val skin: Skin,
    private val assets: Assets
) : UIView() {

    private val actorTabView = ActorTabView(skin, assets)

    override fun create() {
        setup()

//        sceneTabView.create()
        actorTabView.create()

        root.setFillParent(true)

        val menuBarStack = Stack()
//        menuBarStack.add(sceneTabView.menuBar)
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
//        mainViewStack.add(sceneTabView.mainView)
        mainViewStack.add(actorTabView.mainView)
        root.add(mainViewStack).grow()

//        editorEventListener?.let(root::addListener)

//        stage.addActor(root)
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("bg", makePixelTexture(color(10, 23, 36)))
        skin.add("bg-light", makePixelTexture(color(43, 103, 161)))
        skin.add("table-border", color(77, 100, 130))

        skin.add("default", assets.fonts[FontOptions("roboto.ttf", 14f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.getDrawable("bg")
        })

        skin.add("light", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.getDrawable("bg-light")
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

        skin.add("default", ScrollPane.ScrollPaneStyle().apply {
            vScrollKnob = skin.newTintedDrawable("pixel", "button-over")
            vScrollKnob.minWidth = 16f // TODO: use value for resolution independence
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
//        sceneButton.onChanged {
//            if (sceneButton.isChecked) {
//                selectTab(Tab.SCENE)
//            }
//        }

        val actorButton = createButton("Actor")
//        actorButton.onChanged {
//            if (actorButton.isChecked) {
//                selectTab(Tab.ACTOR)
//            }
//        }

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
}
