package bke.iso.editor.event

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import com.badlogic.gdx.scenes.scene2d.Event

sealed class EditorEvent : Event()

data class SelectTilePrefabEvent(val prefab: TilePrefab) : EditorEvent()

data class SelectActorPrefabEvent(val prefab: ActorPrefab) : EditorEvent()

class SelectPointerToolEvent : EditorEvent()

class SelectBrushToolEvent : EditorEvent()

class SelectEraserToolEvent : EditorEvent()

class SaveSceneEvent : EditorEvent()

class OpenSceneEvent : EditorEvent()

class IncreaseLayerEvent : EditorEvent()

class DecreaseLayerEvent : EditorEvent()
