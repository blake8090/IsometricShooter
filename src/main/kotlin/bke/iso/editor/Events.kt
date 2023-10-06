package bke.iso.editor

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
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

sealed class EditorEvent : Event()

data class SelectTilePrefabEvent(val prefab: TilePrefab) : EditorEvent()

data class SelectActorPrefabEvent(
    val prefab: ActorPrefab,
    val texture: String
) : EditorEvent()

class SelectPointerToolEvent : EditorEvent()

class SelectBrushToolEvent : EditorEvent()
