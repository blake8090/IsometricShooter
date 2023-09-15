//package bke.iso.editor
//
//import bke.iso.editor.browser.PrefabData
//import com.badlogic.gdx.scenes.scene2d.Event
//import com.badlogic.gdx.scenes.scene2d.EventListener
//
//interface EditorEventListener : EventListener {
//
//    override fun handle(event: Event?): Boolean {
//        if (event is EditorEvent) {
//            handle(event)
//        }
//        return false
//    }
//
//    fun handle(event: EditorEvent)
//}
//
//sealed class EditorEvent : Event()
//
//data class PrefabSelectedEvent(val prefabData: PrefabData) : EditorEvent()
