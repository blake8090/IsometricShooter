package bke.iso.editor

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener

interface EditorEventListener : EventListener {

    override fun handle(event: Event?): Boolean {
        if (event is EditorEvent) {
            handle(event)
        }
        return false
    }

    fun handle(event: EditorEvent)
}
