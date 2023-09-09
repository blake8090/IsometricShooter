package bke.iso.engine.ui.util

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

fun <T : Button> T.onChanged(action: (T) -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent, actor: Actor) {
            action.invoke(this@onChanged)
        }
    })
}

fun <T : Button> T.onEnter(action: (T) -> Unit) {
    addListener(object : InputListener() {
        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
            action.invoke(this@onEnter)
        }
    })
}

fun <T : Button> T.onExit(action: (T) -> Unit) {
    addListener(object : InputListener() {
        override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
            action.invoke(this@onExit)
        }
    })
}
