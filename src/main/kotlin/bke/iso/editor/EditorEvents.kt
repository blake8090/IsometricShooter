package bke.iso.editor

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.render.Sprite
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

data class TilePrefabSelectedEvent(val prefab: TilePrefab) : EditorEvent()

data class ActorPrefabSelectedEvent(
    val prefab: ActorPrefab,
    val sprite: Sprite
) : EditorEvent()
