package bke.iso.editor.tool

import bke.iso.editor.event.EditorEvent
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab

data class SelectTilePrefabEvent(val prefab: TilePrefab) : EditorEvent()

data class SelectActorPrefabEvent(val prefab: ActorPrefab) : EditorEvent()

class SelectPointerToolEvent : EditorEvent()

class SelectBrushToolEvent : EditorEvent()

class SelectEraserToolEvent : EditorEvent()
