package bke.iso.game.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent

class TextButtonBuilder(text: String, skin: Skin) {

    private var changedAction: (ChangeEvent, Actor) -> Unit = { _, _ -> }
    private var enterAction: (InputEvent, Actor) -> Unit = { _, _ -> }
    private var exitAction: (InputEvent, Actor) -> Unit = { _, _ -> }
    private val button = TextButton(text, skin)

    fun onChanged(action: (ChangeEvent, Actor) -> Unit): TextButtonBuilder {
        changedAction = action
        return this
    }

    fun onEnter(action: (InputEvent, Actor) -> Unit): TextButtonBuilder {
        enterAction = action
        return this
    }

    fun onExit(action: (InputEvent, Actor) -> Unit): TextButtonBuilder {
        exitAction = action
        return this
    }

    fun build(): TextButton {
        button.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                changedAction.invoke(event, actor)
            }
        })

        button.addListener(object : InputListener() {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                enterAction.invoke(event, button)
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                exitAction.invoke(event, button)
            }
        })

        return button
    }
}
