package bke.iso.editor

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

class TextButtonBuilder2(
    private val text: String,
    private val skin: Skin,
    styleName: String? = null
) {
    private var styleName: String? = null
    private var changedAction: (TextButton) -> Unit = {}
//    private var enterAction: (InputEvent, Actor) -> Unit = { _, _ -> }
//    private var exitAction: (InputEvent, Actor) -> Unit = { _, _ -> }

    init {
        this.styleName = styleName
    }

    fun onChanged(action: (TextButton) -> Unit): TextButtonBuilder2 {
        changedAction = action
        return this
    }

//    fun onEnter(action: (InputEvent, Actor) -> Unit): TextButtonBuilder {
//        enterAction = action
//        return this
//    }
//
//    fun onExit(action: (InputEvent, Actor) -> Unit): TextButtonBuilder {
//        exitAction = action
//        return this
//    }

    fun build(): TextButton {
        val button =
            if (styleName != null) {
                TextButton(text, skin, styleName)
            } else {
                TextButton(text, skin)
            }

        button.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                changedAction.invoke(button)
            }
        })

        return button
    }
}

fun textButton(
    text: String,
    skin: Skin,
    styleName: String? = null,
    action: TextButtonBuilder2.() -> Unit = {}
): TextButton {
    val builder = TextButtonBuilder2(text, skin, styleName)
    action.invoke(builder)
    return builder.build()
}
